#@ File    (label = "Input directory", style = "directory") srcDir
#@ File    (label = "Output directory", style = "directory") dstDir
#@ String  (label = "File extension", value=".tif") ext//purposely only use d0.tif as the filename selector.
#@ String  (label = "left side", value = "d0.tif") leftSide
#@ String  (label = "left side", value = "d1.tif") rightSide
#@ Boolean (label = "Keep directory structure when saving", value = true) keepDirectories

import ij.IJ
import ij.ImageStack
import ij.ImagePlus
import ij.plugin.ImageCalculator
import ij.plugin.RGBStackMerge
import ij.process.LUT
import java.awt.Color
import ij.plugin.CompositeConverter

def main() {
	leftList = [];
	rightList = [];
	srcDir.eachFileRecurse {
		name = it.getName().toLowerCase()
		if (name.endsWith(leftSide)) {
			leftList.add(it)
			//process(it, srcDir, dstDir, keepDirectories)
		} else if (name.endsWith(rightSide)) {
			rightList.add(it)
		}
	}
	//since neither list is alphabetically sorted....
	leftList.sort()
	rightList.sort()
	//now they should have the same filename except for the d0 / d1
	for(int i = 0; i < leftList.size(); i++) {
		if(i >= 2) {
			break;
		}
		pairProcess(leftList.get(i), rightList.get(i), srcDir, dstDir, keepDirectories)
	}
	
}


def pairProcess(leftFile, rightFile, sourceDir, destDir, keepDirs) {
//			println("left " + leftFile.getName() + "right" + rightFile.getName())

	left = IJ.openImage(leftFile.getAbsolutePath())
	right = IJ.openImage(rightFile.getAbsolutePath())
	baseName  = leftFile.getName().substring(0, name.length()-6)

	relativePath = keepDirs ?
			sourceDir.toPath().relativize(leftFile.getParentFile().toPath()).toString()
			: "" // no relative path
	saveDir = new File(destDir.toPath().toString(), relativePath)
	if (!saveDir.exists())
		saveDir.mkdirs()


	doMerge(left, right, baseName, saveDir);
	doDiff(left, right, baseName, saveDir);
	

}

def doMerge(left, right, baseName, saveDir) {
	/*
	newLeft = left.clone();
	newRight = right.clone();
	cyan =  LUT.createLutFromColor(Color.CYAN)
	green =  LUT.createLutFromColor(Color.GREEN)
	newLeft.setLut(cyan)
	newRight.setLut(green)
	*/
	    RGBStackMerge stackMerge = new RGBStackMerge();
	    	//ignore luts is false.

	channels = [left, right]
	/*
	if (imp instanceof CompositeImage) {
	    luts = imp.getLuts()
	    luts[0] = LUT.createLutFromColor(Color.CYAN)
	    luts[1] = LUT.createLutFromColor(Color.BLUE)
	    imp.setLuts(luts)
	    imp.updateAndDraw() 
	}*/
	
//this is just putting both images in the same color channel.
//    result = stackMerge.mergeHyperstacks(channels as ImagePlus[], false);
//    result = CompositeConverter.makeComposite(result);
    //TODO ignoring lookup tables due to staticIgnoreLUTs only being set within the mergeStacks macro
	//mergeChannels assumes i've already  done manipulations equal to the blank mergeStacks operation
//	ImageStack startStack = new ImageStack[7]
//	startStack[1] = left
	ImageStack rgb = stackMerge.mergeStacks(left.getWidth(), left.getHeight(), left.getStackSize(), null, right.getStack(), left.getStack(), false)
     result = new ImagePlus("RGB", rgb)
           

    
//	result = stackMerge.mergeStacks(null, left, right, true)
//     result.setDisplayMode(IJ.COLOR)
//     result.flatten()
//	result = run("Merge Channels...", "c2=[" + left + "] c3=[" +right +"] keep")
	merged = new File(saveDir, baseName + "_merged.tif") // customize name if needed
	IJ.saveAs(result, "tiff", merged.absolutePath)

}

def doDiff(left, right, baseName, saveDir) {

	result =  new ImageCalculator().run("Difference create", left,right);
	diffed = new File(saveDir, baseName + "_diff.tif") // customize name if needed

	IJ.saveAs(result, "tif", diffed.absolutePath)

}

main()
