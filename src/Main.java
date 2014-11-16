import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.omg.CORBA.portable.ApplicationException;

public class Main {

	public static final String EXIF = 			"exif";
	public static final String JFIF = 			"jfif";
	public static final String SAMSUNG = 		"sams";
	public static final String SONYERICSSON = 	"sony eri";
	public static final String SONY = 			"sony";
	public static final String IPHONE =			"iphone";
	public static final String CANON = 			"canon";
	public static final String CASIO =			"casio";
	public static final String OLYMPUS =		"olympus";
	public static final String NIKON =			"nikon";
	public static final String HP =				"hp";

	public static final String UNKNOWN_BRAND = "UNKNOWN BRAND";
	public static final String UNKNOWN_DATE = "UNKNOWN DATE";

	public static int cc = 0;

	private static final boolean printBytes = true;

	// SERVER
	//public static String PIX_FOLDER = "/media/storage/new2TB/Video/picts/";
	//public static final String UNKNOWN_FORMAT_DEST_FOLDER = "/media/storage/new2TB/Video/unknown/";

	// LOCAL
	public static final String PIX_FOLDER = "/home/hani/Desktop/PIX/";
	public static final String UNKNOWN_FORMAT_DEST_FOLDER = "/home/hani/Desktop/unknown/";

	public static void main(String[] args) {

		/*
		String root_s = "/media/storage";
		File root_f = new File(root_s);

		System.out.println(jpegCount(root_f));
		 */

		File f = new File(PIX_FOLDER);
		for (File ff : f.listFiles())
			JPEGHeader(ff);

	}

	public static void JPEGHeader(File f) {

		if (!isJPEG(f))
			return;
		try {
			FileInputStream fis = new FileInputStream(f);
			byte[] allData = new byte[(int) f.length()];
			fis.read(allData);
			fis.close();

			String format = getFormat(allData);
			byte[] data = new byte[3072];

			System.arraycopy(allData, 0, data, 0, data.length);

			/*
			String brand = getBrand(data, f, true);
			System.out.println(
					f.getName() + " " +
							format + " " +
							brand + " " +
							getDateX(data)
					);
			 */


			boolean little_endian = false;
			System.out.println("---------------------------------------------------------");

			System.out.println(f.getName());
			
			if ((char)data[12] == 'I') {
				System.out.println("LITLE ENDIAN");
				little_endian = true;
			} else if ((char)data[12] == 'M') {
				System.out.println("BIG ENDIAN");
			}

			String application_marker = toHex(new byte[]{data[3]});
			if (application_marker.equals("E0")) {
				System.out.println("JFIF!");
				//System.out.println("Application Marker: JFIF");
				/*
				File fout = new File(UNKNOWN_FORMAT_DEST_FOLDER + f.getName());
				try {
					Files.copy(f.toPath(), fout.toPath());
				} catch (Exception e) {

				}
				return;
				*/
			} else if (application_marker.equals("E1")) {
				//System.out.println("Application Marker: EXIF");
			}

			String marker_size_s = toHex(new byte[]{data[4], data[5]});
			int marker_size = Integer.parseInt(marker_size_s, 16); // bytes

			if (printBytes) {
				for (int i=0; i<data.length; i++) {
					byte b = data[i];
					if ( (int)b > 32 ){
						//System.out.print((char)b);
						System.out.print("("+i+")" + (char)b);
					}
				}
				System.out.println();
			}
			
			/*
			System.out.println("SOI: " + toHex(getBytes(data, 0, 2)));
			System.out.println("APP0 Marker: " + toHex(getBytes(data, 2, 2)));
			System.out.println("Length: " + 
					toHex(getBytes(data, 4, 2)) + " " +
					Integer.parseInt(toHex(getBytes(data, 4, 2)), 16) + " bytes");
			System.out.println("Identifier: " + toHex(getBytes(data, 6, 5)));
			System.out.println("Version: " + toHex(getBytes(data, 11, 2)));
			System.out.println("Density Units: " + toHex(getBytes(data, 13, 1)));
			System.out.println("X density: " + toHex(getBytes(data, 14, 2)) + " " +
					Integer.parseInt(toHex(getBytes(data, 14, 2)), 16) + " pix"
					);
			System.out.println("Y density: " + toHex(getBytes(data, 16, 2))+ " " +
					Integer.parseInt(toHex(getBytes(data, 16, 2)), 16) + " pix"
					);
			System.out.println("Thumbnail width: " + toHex(getBytes(data, 18, 1)));
			System.out.println("Thumbnail height: " + toHex(getBytes(data, 19, 1)));
			
			int j = 0;
			for (int i=0; i<256; i++) {
				j++;
				byte[] d = new byte[1];
				d[0] = data[i];
				String hex = toHex(d);
				System.out.print(hex + " ");
				if (j%16==0)
					System.out.println();
				else if (j%4==0)
					System.out.print(" ");
			}
			*/
			
			int j = 0;
			int offset = 0;
			System.out.println("Marker size: " + marker_size);
			System.out.print(j + ": ");
			for (int i=0; i<196+4; i+=2) {
				j++;

				byte[] d = new byte[2];
				d[0] = data[i];
				d[1] = data[i+1];

				String hex = toHex(d);

				if (printBytes) {
					System.out.print(hex + " ");
					if (j%6==0) {
						System.out.println();
						//System.out.print(j*2 + ": ");
					}
				}

				if (little_endian) { // LITTLE ENDIAN
					// Make
					if (hex.equals("0F01")) {
						offset = getOffset(data, i, LITTLE_ENDIAN) + 12;
						System.out.println("Make: " + getString(data, offset, 10));
					}
					// Model
					else if (hex.equals("1001")) {
						offset = getOffset(data, i, LITTLE_ENDIAN) + 12;
						System.out.println("Model: " + getString(data, offset, 10));
					}
					// DateTime
					else if (hex.equals("3201")) {
						offset = getOffset(data, i, LITTLE_ENDIAN) + 12;
						System.out.println("DateTime: " + getString(data, offset, 10));
					}
					else if (hex.equals("1A01")) { // XResolution						
						offset = Integer.parseInt(
								toHex(toLittle(getBytes(data, i+8, 4))), 
								16);
						byte[] num = getBytes(data, offset+12, 4);
						byte[] den = getBytes(data, offset+12+4, 4);
						int inum = Integer.parseInt(toHex(num),16);
						int iden = Integer.parseInt(toHex(den),16);
						System.out.println("XRes: " + inum/iden);				
					}
					else if (hex.equals("1B01")) { // YResolution						
						offset = Integer.parseInt(
								toHex(toLittle(getBytes(data, i+8, 4))), 
								16);
						byte[] num = getBytes(data, offset+12, 4);
						byte[] den = getBytes(data, offset+12+4, 4);
						int inum = Integer.parseInt(toHex(num),16);
						int iden = Integer.parseInt(toHex(den),16);
						System.out.println("YRes: " + inum/iden);				
					}
				} else { // BIG ENDIAN
					
					// Make
					if (hex.equals("010F")) {
						offset = getOffset(data, i, BIG_ENDIAN) + 12;
						System.out.println("Make: " + getString(data, offset, 10));
					}
					// Model
					else if (hex.equals("0110")) {
						offset = getOffset(data, i, BIG_ENDIAN) + 12;
						System.out.println("Model: " + getString(data, offset, 10));
					}
					// DateTime
					else if (hex.equals("0132")) {
						offset = getOffset(data, i, BIG_ENDIAN) + 12;
						System.out.println("DateTime: " + getString(data, offset, 10));
					}
					else if (hex.equals("0131")) {
						offset = getOffset(data, i, BIG_ENDIAN) + 12;
						System.out.println("Software: " + getString(data, offset, 10));
					}
					else if (hex.equals("011A")) { // XResolution						
						offset = Integer.parseInt(
								toHex(getBytes(data, i+8, 4)), 
								16);
						byte[] num = getBytes(data, offset+12, 4);
						byte[] den = getBytes(data, offset+12+4, 4);
						int inum = Integer.parseInt(toHex(num),16);
						int iden = Integer.parseInt(toHex(den),16);
						System.out.println("XRes: " + inum/iden);
					}
					else if (hex.equals("011B")) { // YResolution						
						offset = Integer.parseInt(
								toHex(getBytes(data, i+8, 4)), 
								16);
						byte[] num = getBytes(data, offset+12, 4);
						byte[] den = getBytes(data, offset+12+4, 4);
						int inum = Integer.parseInt(toHex(num),16);
						int iden = Integer.parseInt(toHex(den),16);
						System.out.println("YRes: " + inum/iden);						
					}
				}
			}
			System.out.println("---------------------------------------------------------");			
		} catch (Exception e) {

		}
	}

	private static final int LITTLE_ENDIAN = 0;
	private static final int BIG_ENDIAN = 1;
	
	private static byte[] toLittle(byte[] d) {
		byte[] l = new byte[d.length];
		for (int i=0; i<d.length; i++)
			l[i] = d[d.length-1-i];
		return l;
	}

	private static int getOffset(byte[] data, int index, int endian) {
		byte[] d = new byte[2];
		if (LITTLE_ENDIAN == endian) {
			d[1] = data[index+8];
			d[0] = data[index+9];
		} else {
			d[0] = data[index+10];
			d[1] = data[index+11];
		}
		return Integer.parseInt(toHex(d), 16);
	}

	public static boolean isCanon(byte[] b) {
		return isBrand(
				b, 
				new int[]{134,140,158,250,2248,2260},
				5, CANON);
	}

	public static boolean isIPhone(byte[] b) {
		return isBrand(
				b, 
				new int[]{140,152,158,164,170,262,2200,2212,2230,2254,2242},
				6, IPHONE);
	}

	public static boolean isSony(byte[] b) {
		return isBrand(
				b, 
				new int[]{158,170,190,262,2248,2292},
				4, SONY);
	}

	public static boolean isCasio(byte[] b) {
		return isBrand(
				b, 
				new int[]{158,2230,2248}, 
				5, CASIO);
	}

	public static boolean isSamsung(byte[] b) {
		return isBrand(
				b, 
				new int[]{134,170,185,203,2275,2224,2242,2260}, 
				4, SAMSUNG);
	}

	public static boolean isSonyEricsson(byte[] b) {
		return isBrand(
				b, 
				new int[]{146,158,164,206,272,548,560,2230,2248,2236}, 
				8, SONYERICSSON);
	}

	public static boolean isOlympus(byte[] b) {
		return isBrand(
				b, 
				new int[]{188,1104},
				7, OLYMPUS);
	}

	public static boolean isNikon(byte[] b) {
		return isBrand(
				b, 
				new int[]{188},
				5, NIKON);
	}

	public static boolean isHP(byte[] b) {
		return isBrand(
				b, 
				new int[]{30},
				2, HP);
	}

	private static boolean isBrand(byte[] b, int[] startIndecis, int length, String brandName) {
		for (int s : startIndecis) {
			String brand = getString(b, s, length);
			if (brand.equalsIgnoreCase(brandName))
				return true;
		}
		return false;
	}

	public static String getDateX(byte[] b) {
		for (int i=0; i<b.length; i++) {
			String year = getString(b, i, 5);
			if (isValidYear(year))
				return getString(b, i, 20);
		}
		return "";
	}

	private static boolean isValidYear(String year) {
		for (String s : VALID_YEAR)
			if ((s+":").equals(year))
				return true;
		return false;
	}

	private static final String[] VALID_YEAR = new String[]
			{"2000", "2001", "2002", "2003", "2004", "2005",
		"2006", "2007", "2008", "2009", "2010",
		"2011", "2012", "2013","2014", "2015"};

	public static String getString(byte[] d, int start, int length) {
		if (d.length < start)
			return "";
		byte[] b = new byte[length];
		System.arraycopy(d, start, b, 0, length);
		return getString(b);
	}
	
	private static byte[] getBytes(byte[] d, int start, int length) {
		if (d.length < start)
			return null;
		byte[] b = new byte[length];
		System.arraycopy(d, start, b, 0, length);
		return b;
	}

	public static String getString(byte[] d) {
		String s = "";
		for (byte b : d)
			s += (char)b + "";
		return s;
	}

	public static String getFormat(byte[] b) {
		String format = (char)b[6] + "" + 
				(char)b[7] + "" + 
				(char)b[8] + "" + 
				(char)b[9] + "";
		return format;
	}

	private static final int x = 0;
	public static String getBrand(byte[] b, File fin, boolean writeUnknownToFolder) {
		if (isSamsung(b)) return "SAMSUNG";
		if (isSony(b)) return "SONY";
		if (isSonyEricsson(b)) return "SONYERICSSON";
		if (isIPhone(b)) return "IPHONE";
		if (isCanon(b)) return "CANON";
		if (isCasio(b)) return "CASIO";
		if (isOlympus(b)) return "OLYMPUS";
		if (isNikon(b)) return "NIKON";
		if (isHP(b)) return "HP";
		if (writeUnknownToFolder) {
			//File fout = new File(UNKNOWN_FORMAT_DEST_FOLDER + (++x) + "." + getExtension(fin));
			File fout = new File(UNKNOWN_FORMAT_DEST_FOLDER + fin.getName());
			try {
				Files.copy(fin.toPath(), fout.toPath());
			} catch (Exception e) {

			}
		}
		return UNKNOWN_BRAND;
	}

	public static int jpegCount(File f) {
		int c = 0;
		for (File ff : f.listFiles()) {
			if (!ff.isDirectory()) {
				if (isJPEG(ff) && ff.length() > 512*1024) {
					c++;
					System.out.println(++cc + " " + ff.getName());
					try {
						Files.copy(ff.toPath(),
								(new File(PIX_FOLDER + cc + "." + getExtension(ff))).toPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else
				c += jpegCount(ff); 
		}
		return c;
	}

	public static boolean isJPEG(File file) {
		return 
				getExtension(file).equals("jpeg") ||
				getExtension(file).equals("JPEG") ||
				getExtension(file).equals("jpg") ||
				getExtension(file).equals("JPG");
	}

	public static String getExtension(File file) {
		String ext = "";
		try {
			ext = file.getName().split("\\.", -1)[1];
		} catch (Exception e) {

		}
		return ext;
	}

	private static final char[] hexArray =
			"0123456789ABCDEF".toCharArray();

	private static String toHex(byte[] b) {
		char[] hexChars = new char[b.length*2];
		for (int j=0; j<b.length; j++) {
			int v = b[j] & 0xFF;
			hexChars[j*2] = hexArray[v >>> 4];
			hexChars[j*2+1] = hexArray[v&0x0F];
		}
		return new String(hexChars);
	}

}
