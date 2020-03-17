@echo off
SETLOCAL ENABLEEXTENSIONS
SET me=%~n0
SET parent=%~dp0

REM *** THIS SCRIPT REQUIRES THE CMFT.EXE FROM https://github.com/dariomanesku/cmft
REM *** PUT IT IN THE SAME FOLDER AS THIS SCRIPT OR ADD IT TO THE PATH

REM *************************************************************************
REM ********************* YOU CAN CHANGE THESE SETTINGS *********************
REM *************************************************************************
SET input_file="simons_town_rocks_4k.hdr"
SET output_folder=out
SET exposure=2.0
SET edge_fixup=warp


REM *************************************************************************
REM ******************** OPTIONALLY CHANGE THESE SETTINGS *******************
REM *************************************************************************
REM **********************INTERNAL VARIABLE ASSIGNMENTS**********************
REM *************************************************************************
SET gamma_params=--inputGammaNumerator 1.0 --inputGammaDenominator 1.0 --outputGammaNumerator 1.0 --outputGammaDenominator %exposure%


REM PRINT SETTINGS
echo Starting the execution with the following settings:
echo Input File: %input_file%
echo Output Folder: %output_folder%
echo Exposure: %exposure%
echo Edge Fixup: %edge_fixup%
echo.

REM GENERATE ENVIRONMENT MAPS
echo Generating environment maps...
cmft --input %input_file% --filter none %gamma_params% --dstFaceSize 1024 --outputNum 1 --output0 %output_folder%/environment/environment --output0params tga,bgra8,facelist
echo Environment maps generated!
echo.

REM GENERATE IRRADIANCE MAPS
echo Generating irradiance maps...
cmft --input %input_file% --filter irradiance %gamma_params% --dstFaceSize 128 --outputNum 1 --output0 %output_folder%/diffuse/diffuse --output0params tga,bgra8,facelist
echo Irradiance maps generated!
echo.

REM GENERATE RADIANCE MAPS
echo Generating radiance maps...
cmft --input %input_file% --filter radiance  %gamma_params% --srcFaceSize 512 --excludeBase false --mipCount 10 --glossScale 12 --glossBias 1 --lightingModel phongbrdf --dstFaceSize 512 --numCpuProcessingThreads 4 --useOpenCL true --clVendor anyGpuVendor --deviceType gpu --deviceIndex 0 --generateMipChain false --edgeFixup %edge_fixup% --outputNum 1 --output0 %output_folder%/specular/specular --output0params tga,bgra8,facelist
echo Radiance maps generated!
echo.

pause