#@ File    (label = "Input directory", style = "directory") srcDir
#@ File    (label = "Output directory", style = "directory") dstDir
#@ String  (label = "File extension", value=".tif") ext
#@ String  (label = "Green naming pattern", value = "d0.tif") greenSide
#@ String  (label = "Blue naming pattern", value = "d1.tif") blueSide
#@ Boolean (label = "this field not implmemented", value = true) keepDirectories

import ij.IJ
import ij.ImageStack
import ij.ImagePlus
import ij.plugin.ImageCalculator
import ij.plugin.RGBStackMerge
import ij.process.LUT
import java.awt.Color
import ij.plugin.CompositeConverter
/*
 * Iterate over a folder of images from a CX7 output.
 * These will be TIFs with identical base names except the last digits before the .TIF: d0, d1, o1
 * o1 is ignored, we want to create composites of the d0 and d1 images. 
 * 
 * 
 */
def main() {
	greenList = [];
	blueList = [];
	srcDir.eachFileRecurse {
		name = it.getName().toLowerCase()
		if (name.endsWith(greenSide)) {
			greenList.add(it)
		} else if (name.endsWith(blueSide)) {
			blueList.add(it)
		}
	}
	greenList.sort()
	blueList.sort()
	//iterating over both lists in order numerically should be the correct pairing.
	for(int i = 0; i < greenList.size(); i++) {
		pairProcess(greenList.get(i), blueList.get(i), srcDir, dstDir, keepDirectories)
	}
	
}


def pairProcess(greenFile, blueFile, sourceDir, destDir, keepDirs) {
	if(greenFile.getName().substring(0, name.length()-6) != blueFile.getName().substring(0, name.length()-6)) {
		println("Not the same base image name for merging, don't merge. green image: " + greenFile.getName() + " -- blue image:" + blueFile.getName());
		return;
	}
	greenImage = IJ.openImage(greenFile.getAbsolutePath())
	blueImage = IJ.openImage(blueFile.getAbsolutePath())
	baseName  = greenFile.getName().substring(0, name.length()-6)

//relativePAth is useless here at the moment.
	relativePath = keepDirs ?
			sourceDir.toPath().relativize(greenFile.getParentFile().toPath()).toString()
			: "" // no relative path
	saveDir = new File(destDir.toPath().toString(), relativePath)
	if (!saveDir.exists())
		saveDir.mkdirs()


	doMerge(greenImage, blueImage, baseName, saveDir);
	doDiff(greenImage, blueImage, baseName, saveDir);
	

}

/**
 * perform a stack merge on the two images, producing a green/blue composite image.
 */
def doMerge(greenImage, blueImage, baseName, saveDir) {
	RGBStackMerge stackMerge = new RGBStackMerge();
	ImageStack rgb = stackMerge.mergeStacks(greenImage.getWidth(), greenImage.getHeight(), greenImage.getStackSize(), null, greenImage.getStack(), blueImage.getStack(), false)
    result = new ImagePlus("RGB", rgb)
	merged = new File(saveDir, baseName + "_merged.tif") 
	IJ.saveAs(result, "tiff", merged.absolutePath)

}
/**
 * produce a image solely of the grayscale difference between the two images, absolute value
 */
def doDiff(greenImage, blueImage, baseName, saveDir) {
	result =  new ImageCalculator().run("Difference create", greenImage, blueImage);
	diffed = new File(saveDir, baseName + "_diff.tif")
	IJ.saveAs(result, "tif", diffed.absolutePath)
}

main()
