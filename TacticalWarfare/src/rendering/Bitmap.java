package rendering;

public class Bitmap {
	
	static Texture bitmap_texture = new Texture("numbers.png");

	double[] vertices = new double[8];
	double[] textureCoords = new double[8];
	
	Model box = new Model(vertices, textureCoords, new int[]{0, 1, 2, 2, 1, 3});
	
	double letter_width = 6;
	double letter_height = 7;
	
	double image_width = 69;
	double image_height = 7;
	
	public void drawLetter(int x1, int y1, int x2, int y2, int letter) {
		setTextureCoords(letter);
		box.render(x1, y1, x2, y2);
	}
	
	public void drawNumber(int x1, int y1, int x2, int y2, int number) {
		bitmap_texture.bind();
		int width = x2 - x1;
		String digits = Integer.toString(number);
		for (int i = 0; i < digits.length(); i++) {
			int offset = i * width;
			drawLetter(x1 + offset + 5, y1, x2 + offset, y2, Character.getNumericValue(digits.charAt(i)));
		}
	}
	
	public void setTextureCoords(int letter) {
		textureCoords = new double[] {(letter * 7)/image_width, 0, (letter * 7)/image_width, 1, (letter * 7 + 6)/image_width, 0, (letter * 7 + 6)/image_width, 1}; 
		box.setTextureCoords(textureCoords);
	}

	


}
