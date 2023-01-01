package net.mgsx.gltf.demo.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.IOException;

public abstract class SafeHttpResponseListener implements HttpResponseListener {

  @Override
  public void handleHttpResponse(HttpResponse httpResponse) {
    try {
      final byte[] bytes = StreamUtils.copyStreamToByteArray(httpResponse.getResultAsStream());
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

  protected abstract void handleData(byte[] bytes);

  protected abstract void handleError(Throwable t);

  protected abstract void handleEnd();
}
