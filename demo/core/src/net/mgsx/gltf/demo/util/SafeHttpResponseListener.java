package net.mgsx.gltf.demo.util;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.utils.StreamUtils;

abstract public class SafeHttpResponseListener implements HttpResponseListener
{
	@Override
	public void handleHttpResponse(HttpResponse httpResponse) {
		try {
			final byte [] bytes = StreamUtils.copyStreamToByteArray(httpResponse.getResultAsStream());
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					handleData(bytes);
					handleEnd();
				}
			});
		} catch (IOException e) {
			failed(e);
		}
	}

	@Override
	public void failed(final Throwable t) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				handleError(t);
				handleEnd();
			}
		});
	}

	@Override
	public void cancelled() {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				handleEnd();
			}
		});
	}
	
	abstract protected void handleData(byte [] bytes);
	abstract protected void handleError(Throwable t);
	abstract protected void handleEnd();

}
