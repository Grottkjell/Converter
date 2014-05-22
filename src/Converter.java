import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Converter {
	private StringBuilder 			mXmlOutput;
	private String 					mDeliminator;
	private ArrayList<String> 		mTextFileInput;
	private BufferedReader 			mBufferedReader;
	private InfoType 				mCurrentParentType;
	private int 					mCurrentIndentation;
	
	public static final String[] ERROR_MESSAGE_INVALID_ROW 		= {"Warning, row ", " is incorrect. Control input file.\n"};
	public static final String[] ERROR_MESSAGE_INVALID_FILE		= {"Warning, file ", " is incorrect. Use valid file format: .", " \n"};
	
	public static final String DEFAULT_DELIMITER 	= "\\|";
	public static final String DOT 					= "\\.";
	public static final String TAB					= "\t";
	public static final String TXT_FILE_EXTENSION	= "txt";
	public static final String XML_FILE_EXTENSION	= "xml";
		
	public static final String TAG_TXT_PERSON 		= "P";
	public static final String TAG_TXT_ADDRESS 		= "A";
	public static final String TAG_TXT_PHONE 		= "T";
	public static final String TAG_TXT_FAMILY 		= "F";
	
	public static final String TAG_XML_ROOT			= "people";
	public static final String TAG_XML_PERSON 		= "person";
	public static final String TAG_XML_FIRST_NAME	= "firstname";
	public static final String TAG_XML_LAST_NAME	= "lastname";
	
	public static final String TAG_XML_ADDRESS 		= "address";
	public static final String TAG_XML_STREET 		= "street";
	public static final String TAG_XML_CITY 		= "city";
	public static final String TAG_XML_ZIP	 		= "zipcode";
	
	public static final String TAG_XML_PHONE 		= "phone";
	public static final String TAG_XML_MOBILE 		= "mobile";
	public static final String TAG_XML_HOMEPHONE	= "homephone";
	
	public static final String TAG_XML_FAMILY 		= "family";
	public static final String TAG_XML_BORN 		= "born";
	
	public Converter(){
		mXmlOutput = new StringBuilder();
		mDeliminator = new String(DEFAULT_DELIMITER);
		mTextFileInput = new ArrayList<String>();
		mCurrentParentType = InfoType.UNKNOWN_INFO;
		mCurrentIndentation = 0;
	}
	
	public Converter(String deliminator){
		mXmlOutput = new StringBuilder();
		mDeliminator = deliminator;	
		mTextFileInput = new ArrayList<String>();
		mCurrentParentType = InfoType.UNKNOWN_INFO;
		mCurrentIndentation = 0;
	}
	
	public boolean readTextFile(String fileName){
		//Check if the name of the input file is in the correct format.
		if( validInputFile(fileName, TXT_FILE_EXTENSION) ){
			try{
				mBufferedReader = new BufferedReader( new FileReader(fileName) );
			}catch(FileNotFoundException e){
				System.out.println(e.getMessage());
				return false;
			}
		}else{
			return false;
		}		
		String currentLine = null;
		try{
			while( (currentLine = mBufferedReader.readLine()) != null){
				mTextFileInput.add(currentLine);
			}
			mBufferedReader.close();
		}catch(IOException e) {
			System.out.println(e.getMessage());
			return false;
		}		
		return true;
	}
	
	public void ConvertToXmlFormat(){
		String currentLine = null;
		String[] currentLineParts = null;
		InfoType currentRowInfoType = null;
		boolean reportInvalidRow;
		appendOpeningTag(TAG_XML_ROOT);
		mCurrentIndentation++;
		//Iterate over the input and create corresponding output.
		for(int i = 0; i < mTextFileInput.size(); i++){
			currentLine = mTextFileInput.get(i);
			currentLineParts = currentLine.split(mDeliminator);
			currentRowInfoType = checkRowInfoType(currentLineParts);
			reportInvalidRow = false;						
			switch(currentRowInfoType){
			case PERSON:				
				if(mCurrentParentType == InfoType.FAMILY){
					closeFamilyTag();
					closePersonTag();
					addPerson(currentLineParts);
				}else if(mCurrentParentType == InfoType.PERSON){
					closePersonTag();
					addPerson(currentLineParts);
				}else if(mCurrentParentType == InfoType.UNKNOWN_INFO){
					addPerson(currentLineParts);
				}else{
					reportInvalidRow = true;
				}
				break;				
			case PHONE_NUMBERS:
				if(mCurrentParentType == InfoType.FAMILY || mCurrentParentType == InfoType.PERSON){
					addPhoneNumbers(currentLineParts);
				}else{
					reportInvalidRow = true;
				}
				break;				
			case ADDRESSES:
				if(mCurrentParentType == InfoType.FAMILY || mCurrentParentType == InfoType.PERSON){
					addAddress(currentLineParts);
				}else{
					reportInvalidRow = true;
				}
				break;				
			case FAMILY:
				if(mCurrentParentType == InfoType.PERSON){
					addFamily(currentLineParts);
				}else if(mCurrentParentType == InfoType.FAMILY){
					closeFamilyTag();
					addFamily(currentLineParts);
				}else{
					reportInvalidRow = true;
				}				
				break;				
			case UNKNOWN_INFO:
				reportInvalidRow = true;
				break;
			}			
			if(reportInvalidRow){
				System.out.println(ERROR_MESSAGE_INVALID_ROW[0] + (i + 1) + ERROR_MESSAGE_INVALID_ROW[1] );
			}
		}		
		//Close tags that are open after iteration.
		switch(mCurrentParentType){
		case FAMILY: //Fall-through case, since a Family-tag has to follow a Person-tag.
			mCurrentIndentation--;
			appendClosingTagOnNewLine(TAG_XML_FAMILY);
		case PERSON:
			mCurrentIndentation--;
			appendClosingTagOnNewLine(TAG_XML_PERSON);
			break;
		case ADDRESSES:
			break;
		case PHONE_NUMBERS:
			break;
		case UNKNOWN_INFO:
			break;
		default:
			break;		
		}		
		mCurrentIndentation--;
		appendClosingTagOnNewLine(TAG_XML_ROOT);
	}
	
	public boolean writeToFile(String fileName){
		boolean result = true;		
		if(validInputFile(fileName, XML_FILE_EXTENSION)){
			try{
				BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter
						(new FileOutputStream(fileName), "utf-8"));
				bufferedWriter.write(mXmlOutput.toString());
				bufferedWriter.close();
			}catch(IOException e){
				System.out.println(e.getMessage());
				result = false;
			} 	
		}else{
			result = false;
		}		
		return result;
	}
	
	public boolean validInputFile(String fileName, String extension){
		boolean fileIsValid = false;		
		String[] inputFileParts = fileName.split(DOT);		
		if( inputFileParts.length == 2 && (inputFileParts[inputFileParts.length - 1].equals(extension)) ){
			fileIsValid = true;
		}else{
			System.out.println(ERROR_MESSAGE_INVALID_FILE[0] + fileName + ERROR_MESSAGE_INVALID_FILE[1] 
					+ extension + ERROR_MESSAGE_INVALID_FILE[2]);
		}		
		return fileIsValid;
	}
	
	/*
	 * Identifies and validates if the given row corresponds to a specific type of information.
	 * Returns InformationType.UnknownInfo if no specific type of information can be identified.
	 */
	private InfoType checkRowInfoType(String[] rowParts){
		InfoType infoType = InfoType.UNKNOWN_INFO;
		if(rowParts.length == 3){
			String indicator = rowParts[0].toUpperCase();	
			switch(indicator){
			case TAG_TXT_PERSON:
				infoType = InfoType.PERSON;
				break;
			case TAG_TXT_PHONE:
				infoType = InfoType.PHONE_NUMBERS;
				break;
			case TAG_TXT_ADDRESS:
				infoType = InfoType.ADDRESSES;
				break;
			case TAG_TXT_FAMILY:
				infoType = InfoType.FAMILY;
				break;
			}
		}else if(rowParts.length == 4){
			String indicator = rowParts[0].toUpperCase();	
			if(indicator.equals(TAG_TXT_ADDRESS)){
				infoType = InfoType.ADDRESSES;
			}
		}
		return infoType;
	}
	
	private void addPerson(String[] lineSplit){
		appendOpeningTagOnNewLine(TAG_XML_PERSON);
		mCurrentIndentation++;
		appendOpeningTagOnNewLine(TAG_XML_FIRST_NAME);
		appendInfo(lineSplit[1]);
		appendClosingTag(TAG_XML_FIRST_NAME);
		appendOpeningTagOnNewLine(TAG_XML_LAST_NAME);
		appendInfo(lineSplit[2]);
		appendClosingTag(TAG_XML_LAST_NAME);
		mCurrentParentType = InfoType.PERSON;
	}
	
	private void addPhoneNumbers(String[] lineSplit){
		appendOpeningTagOnNewLine(TAG_XML_PHONE);
		mCurrentIndentation++;
		appendOpeningTagOnNewLine(TAG_XML_MOBILE);
		appendInfo(lineSplit[1]);
		appendClosingTag(TAG_XML_MOBILE);
		appendOpeningTagOnNewLine(TAG_XML_HOMEPHONE);
		appendInfo(lineSplit[2]);
		appendClosingTag(TAG_XML_HOMEPHONE);
		mCurrentIndentation--;
		appendClosingTagOnNewLine(TAG_XML_PHONE);
	}
	
	private void addFamily(String[] lineSplit){
		appendOpeningTagOnNewLine(TAG_XML_FAMILY);
		mCurrentIndentation++;
		appendOpeningTagOnNewLine(TAG_XML_FIRST_NAME);
		appendInfo(lineSplit[1]);
		appendClosingTag(TAG_XML_FIRST_NAME);
		appendOpeningTagOnNewLine(TAG_XML_BORN);
		appendInfo(lineSplit[2]);
		appendClosingTag(TAG_XML_BORN);
		mCurrentParentType = InfoType.FAMILY;
	}
	
	private void addAddress(String[] lineSplit){
		appendOpeningTagOnNewLine(TAG_XML_ADDRESS);
		mCurrentIndentation++;
		appendOpeningTagOnNewLine(TAG_XML_STREET);
		appendInfo(lineSplit[1]);
		appendClosingTag(TAG_XML_STREET);
		appendOpeningTagOnNewLine(TAG_XML_CITY);
		appendInfo(lineSplit[2]);
		appendClosingTag(TAG_XML_CITY);
		if(lineSplit.length > 3){
			appendOpeningTagOnNewLine(TAG_XML_ZIP);
			appendInfo(lineSplit[3]);
			appendClosingTag(TAG_XML_ZIP);
		}
		mCurrentIndentation--;
		appendClosingTagOnNewLine(TAG_XML_ADDRESS);
	}
	
	private void closePersonTag(){
		mCurrentIndentation--;
		appendClosingTagOnNewLine(TAG_XML_PERSON);
	}
	
	private void closeFamilyTag(){
		mCurrentIndentation--;
		appendClosingTagOnNewLine(TAG_XML_FAMILY);
	}
	
	private void appendOpeningTag(String tag){
		mXmlOutput.append("<" + tag + ">");
	}
	
	private void appendOpeningTagOnNewLine(String tag){
		mXmlOutput.append(System.getProperty("line.separator"));
		appendIndentation();
		mXmlOutput.append("<" + tag + ">");
	}
	
	private void appendClosingTag(String tag){
		mXmlOutput.append("</" + tag + ">");
	}
	
	private void appendClosingTagOnNewLine(String tag){
		mXmlOutput.append(System.getProperty("line.separator"));
		appendIndentation();
		mXmlOutput.append("</" + tag + ">");
	}
	
	private void appendInfo(String info){
		mXmlOutput.append(info);
	}
	
	private void appendIndentation(){
		for(int i = 0; i < mCurrentIndentation; i++){
			mXmlOutput.append(TAB);
		}
	}

	public String getmDeliminator(){
		return mDeliminator;
	}

	public void setmDeliminator(String mDeliminator){
		this.mDeliminator = mDeliminator;
	}
}
