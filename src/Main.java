import java.util.Scanner;

public class Main {

	public static void main(String[] args) {	
		String fileName = "test1.txt";	
		Converter converter = new Converter();
		Scanner scanner = new Scanner(System.in);
		do{
			System.out.println("Enter name of the textfile to convert (Format: xxx.txt):");
			fileName = scanner.nextLine();
		} while ( !converter.readTextFile(fileName) );
		System.out.println("Converting textfile...");		
		converter.ConvertToXmlFormat();
		System.out.println("Textfile converted.");
		do{
			System.out.println("Enter name of the xmlfile to create(Format: xxx.xml):");
			fileName = scanner.nextLine();
		}while ( !converter.writeToFile(fileName) );
		scanner.close();
		System.out.println("Xmlfile created.");
	}

}
