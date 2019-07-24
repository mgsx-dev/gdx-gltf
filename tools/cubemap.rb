
# require cmft (CubeMap Filtering Tool), see https://github.com/dariomanesku/cmft
# Linux : download and extract binaries and copy cmft executable to /usr/bin

# Require imagemagik (convert) in order to convert from TGA to JPG

# Usage : ruby cubemap.rb input_file output_folder

require 'fileutils'

# simple command wrapper to have realtime stdout stream
require 'pty'
def run(cmd)
    begin
      PTY.spawn( cmd ) do |stdout, stdin, pid|
        begin
          stdout.each { |line| print line }
        rescue Errno::EIO
        end
      end
    rescue PTY::ChildExited
    end
end

input_file = ARGV[0] || "simons_town_rocks_4k.hdr"
output_folder = (ARGV[1] || "out")

exposure = 2.0
edgeWrap = true

edge_params = edgeWrap ? "--edgeFixup warp" : "--edgeFixup none"

gamma_params = "--inputGammaNumerator 1.0 --inputGammaDenominator 1.0 --outputGammaNumerator 1.0 --outputGammaDenominator #{exposure}"

# generate environment map
output_environment_folder = output_folder + "/environment"
output_environment_file = output_environment_folder + "/environment"

FileUtils.makedirs(output_environment_folder)

run "cmft --input #{input_file} --filter none #{gamma_params} --dstFaceSize 1024 --outputNum 1 --output0 #{output_environment_file} --output0params tga,bgra8,facelist"



# generate irrandiance map (diffuse)
output_diffuse_folder = output_folder + "/diffuse"
output_diffuse_file = output_diffuse_folder + "/diffuse"

FileUtils.makedirs(output_diffuse_folder)

run "cmft --input #{input_file} --filter irradiance #{gamma_params} --dstFaceSize 128 --outputNum 1 --output0 #{output_diffuse_file} --output0params tga,bgra8,facelist"


# generate randiance map (specular)
output_specular_folder = output_folder + "/specular"
output_specular_file = output_specular_folder + "/specular"

FileUtils.makedirs(output_specular_folder)

run "cmft --input #{input_file} --filter radiance  #{gamma_params} --srcFaceSize 512 --excludeBase false --mipCount 10 --glossScale 12 --glossBias 1 --lightingModel phongbrdf --dstFaceSize 512 --numCpuProcessingThreads 4 --useOpenCL true --clVendor anyGpuVendor --deviceType gpu --deviceIndex 0 --generateMipChain false #{edge_params} --outputNum 1 --output0 #{output_specular_file} --output0params tga,bgra8,facelist"

# convert to PNG
Dir.glob("#{output_folder}/**/*.tga") do |file|
    run "convert #{file} #{File.dirname(file)}/#{File.basename(file, '.*')}.jpg"
    File.delete(file)
end

# remove size in file names
Dir.glob("#{output_folder}/**/*.jpg") do |file|
    dst = file.gsub(/_[0-9]+x[0-9]+\.jpg$/, '.jpg')
    FileUtils.mv(file, dst) unless file == dst
end

puts "done."

