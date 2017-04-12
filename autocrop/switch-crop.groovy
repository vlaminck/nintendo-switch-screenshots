import java.awt.Image
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

def baseDirectory = "autocrop"
println("baseDirectory: ${baseDirectory}")

def inputDirectory = new File("${baseDirectory}/input")
if (!inputDirectory.exists()) {
  println("Error: Make an input directory and place images in it")
  return
}
def outputDirectory = new File("${baseDirectory}/output")
if (!outputDirectory.exists()) {
  println("Error: Make an output directory for the cropped images to be placed")
  return
}


def crop(String baseDirectory, String inputFileName, int x = 22, int y = 80, int w = 1200, int h = 675) {

  String input = "${baseDirectory}/input/${inputFileName}"

  if (!inputFileName.endsWith(".jpg") && !inputFileName.endsWith(".jpg-large")) {
    println("not cropping ${inputFileName}. File must end in .jpg or .jpg-large")
    return
  }

  println("About to crop $input")
  Image src = ImageIO.read(new File(input));

  BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
  dst.getGraphics().drawImage(src, 0, 0, w, h, x, y, x + w, y + h, null);

  def fileName = inputFileName.split(".jpg")[0]
  String output = "${baseDirectory}/output/${fileName}.png";
  ImageIO.write(dst, "png", new File(output));
  println("Finished writing $output")
}

inputDirectory.eachFileRecurse() { inputFile ->
  def fileName = inputFile.name
  if (fileName.endsWith(".jpg") || fileName.endsWith(".jpg-large")) {
    crop(baseDirectory, fileName)
  }
}
