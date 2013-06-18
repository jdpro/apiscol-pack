package fr.ac_versailles.crdp.apiscol.pack;



import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.SAXException;

import fr.ac_versailles.crdp.apiscol.utils.FileUtils;
import fr.ac_versailles.crdp.apiscol.utils.LogUtility;

public class PackageFile {
	public static String filePathTemporary;
	private static Validator validator;
	private static Logger logger = null;
	private String xsdPath;
	private File fileTemp;
	private File file; //fileValidated --> fichier valider par xsd
	private String fileName; //fileValidatedName. nom du fichier validé (sans son extension). C'est l'uuid enfait
	private String fileNameHierarchy;//the filename but with all its hierarchy
	//packid sera l'uuid.xml

	public PackageFile(String temporaryFilePath){
		if (!PackageFile.isInitialized()){
			initializeFilePathTemporary(temporaryFilePath);
		}
	}




	public static void initializeFilePathTemporary(String filePathTemporary) {
		PackageFile.filePathTemporary = filePathTemporary;
		initializeLogger();
		logger.info("The temporary directory file directory is "
				+ filePathTemporary);
	}

	public void setXsdPath(String xsdPath) {
		this.xsdPath = xsdPath;
		logger.info("The xsdPath is " + xsdPath);
		createValidator(xsdPath);
	}
	
	public String getFileNameHierarchy(){
		return fileNameHierarchy;
	}

	public static boolean isInitialized() {
		return filePathTemporary != null;
	}

	private static void createValidator(String xsdPath){
		SchemaFactory factory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");
		File schemaLocation = new File(xsdPath);
		Schema schema = null;
		try {

			schema = factory.newSchema(schemaLocation);

		} catch (SAXException e1) {
			String m = "unknown";
			switch (PackApi.mode) {
			case IMSLD:
				m = "imsld";
				break;
			case SCORM:
				m = "scorm";
				break;
			default:
				m = "unknown";
			}
			logger.error("The " + m + " xsd files seems to be corrupted");
		}

		validator = schema.newValidator();
		//System.out.println(validator.toString());
	}

	private static void initializeLogger() {
		if (logger == null)
			logger = LogUtility.createLogger(PackageFile.class
					.getCanonicalName());
		//System.out.println("logger cree");
	}

	public void validateFile() throws InvalidProvidedManifestFileException,
			FileSystemAccessException {
		File scormXml = fileTemp;
		StreamSource source = new StreamSource(scormXml);

		try {
			validator.validate(source);

			String m = "UNKNOWN";
			switch (PackApi.mode) {
			case IMSLD:
				m = "IMSLD";
				break;
			case SCORM:
				m = "SCORM";
				break;
			default:
				m = "UNKNOWN";
			}

			logger.info(scormXml + " is valid as a " + m + " file.");
		} catch (SAXException ex) {
			throw new InvalidProvidedManifestFileException(String.format(
					"The file %s is not valid because %s", scormXml.getName(),
					ex.getMessage()));
		} catch (IOException e) {
			throw new FileSystemAccessException(
					String.format(
							"Impossible to reach the xml file %s when trying to validate",
							scormXml.getName()));
		}

	}

	void temporaryFileCreation() {
		String fileName = "tmp_"+System.currentTimeMillis() + ".xml";
		fileTemp = new File(PackageFile.filePathTemporary
				+ File.separator + fileName);

	}

	void writeInputStreamToFile(InputStream uploadedInputStream) throws IOErrorException{
		String filename="";//juste pour un message d'erreur plus joli...
		try {
			OutputStream out = new FileOutputStream(fileTemp);
			filename=fileTemp.getName();
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			logger.error("pb d'ecriture de fichier");
			throw new IOErrorException("Error while writting the file "+filename);
		}
		logger.info("ecriture de "+filename+" terminée");
	}

	public void renameTempFile() {
		//fileName = UUID.randomUUID().toString();
		//file = new File(PackageFile.filePathTemporary
		//		+ File.separator + fileName + ".xml");
		fileName=UUID.randomUUID().toString();
		File file2=new File(PackageFile.filePathTemporary+ File.separator +fileName.charAt(0)+File.separator+fileName.charAt(1)+File.separator+fileName.charAt(2));
		file2.mkdirs();
		fileNameHierarchy=FileUtils.getFilePathHierarchy("", fileName);
		file = new File(PackageFile.filePathTemporary
				 + fileNameHierarchy + ".xml");
		String hierarchy=retrieveFileNameHierarchy(file);
		try {
			file.createNewFile();
			
		} catch (IOException e) {
			logger.error("Impossible de creer le fichier "+PackageFile.filePathTemporary
				+ fileName + ".xml");
			e.printStackTrace();
		}
		fileTemp.renameTo(file);
		//TODO a revoir ici. Ca doit fonctionner...
		//serializeIntoJSon(fileValidated, fileNameValidated);
		fileTemp.delete();
		logger.info("fichier temp effacé...");
	}

	public File getFileValidated() {
		return file;
	}

	public File getTempFile() {
		return fileTemp;
	}
	
	public String getFileValidatedName(){
		return fileName;
	}

	public static String extractMetadata(String meta) throws IOErrorException {
		Document document = new Document();
		Element racine;
		URL metaURL = null;
		DataInputStream file = null;
		try {
			metaURL = new URL(meta);
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			logger.error("Metadata not found...please ckeck URL...");
		}

		try {//TODO a revoir ici. Ca doit fonctionner...
		//serializeIntoJSon(fileValidated, fileNameValidated); verif ?format=xml peut etre
			file = new DataInputStream(new BufferedInputStream(
					metaURL.openStream()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.error("File " + metaURL + " not found...");
			throw new IOErrorException("impossible to open an inputstream.");
		}

		SAXBuilder sxb = new SAXBuilder();
		try {
			document = sxb.build(file);
		} catch (Exception e) {
		}
		racine = document.getRootElement();
		List listMetadata = racine.getChildren("metaMetadata",
				racine.getNamespace());
		String metadata = "none";

		Iterator ii = listMetadata.iterator();

		while (ii.hasNext()) {

			Element courant = (Element) ii.next();
			metadata = courant.getChild("identifier", racine.getNamespace())
					.getChild("entry", racine.getNamespace()).getText();
			//metadata dans la balise metaMetadata
		}
		return metadata;
	}

	public PackApi.Mode firstAnalyse() {// is the file SCORM type or
												// IMSLD?
		PackApi.Mode mode = PackApi.Mode.UNKNOWN;
		Document document = new Document();
		Element racine;
		SAXBuilder sxb = new SAXBuilder();
		try {
			document = sxb.build(fileTemp);
			} catch (Exception e) {
			e.printStackTrace();
		}
		racine = document.getRootElement();
		List listOrganisations = racine.getChildren("organizations",
				racine.getNamespace());

		if (listOrganisations.size() == 0) {
			logger.info("unknown file type...");
			return mode.UNKNOWN;
		}

		Iterator ii = listOrganisations.iterator();

		while (ii.hasNext()) {

			Element courant = (Element) ii.next();
			if (courant.getChildren().get(0).getName()
					.equals("learning-design")) {
				logger.info("lecture d'un fichier de type IMSLD!!!");
				return mode.IMSLD;
			} else {
				logger.info("lecture d'un fichier de type SCORM...");
				return mode.SCORM;
			}
		}
		return mode.UNKNOWN;

	}
	
	
	private static void serializeIntoJSon(File xmlFile, String UUIDFileName) { //methode inspiree de celle de Joachim ds ResourceDirectoryInterface Mais: notice --> package
		//TODO introduire le getFilePath pour le pb qd nbre fichier >32000 dans un rep
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(xmlFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String xml = null;
		try {
			xml = IOUtils.toString(fis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XMLSerializer xmlSerializer = new XMLSerializer();
		JSON json = xmlSerializer.read(xml);
		//File file = new File(getFilePath(metadataId, true, "js"));
		File file=new File(UUIDFileName+".js");
		writeStringToFile(
				new StringBuilder()
						.append("package(\"")
						.append(xml.replaceAll("[\n\r]", " ").replace("\"",
								"\\\"")).append("\");").toString(), file);

	}
	
	private static void writeStringToFile(String string, File file) {
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(file));
			out.write(string);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static String extractMetadataRestURL(String meta) {
		//System.out.println("Extraction URL rest du fichier de metadata...");
		Document document=new Document();
		Element racine;
		URL metaURL = null;
		DataInputStream file = null;
		try {
			metaURL = new URL(meta+"?format=xml");
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			 e2.printStackTrace();
			logger.info("Metadata introuvable...pb d'URL...");
		}

		try {
			file = new DataInputStream(new BufferedInputStream(
					metaURL.openStream()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.info("Fichier " + metaURL + " non trouvé...");
		}

		
		//System.out.println("debut lecture fichier meta...");
		SAXBuilder sxb = new SAXBuilder();
	
		try {
			document = sxb.build(file);
		} catch (Exception e) {
		}
		racine = document.getRootElement();
		List listMetadata = racine.getChildren("link",
				racine.getNamespace());

		String metaLom = "none";

		Iterator ii = listMetadata.iterator();

		while (ii.hasNext()) {

			Element courant = (Element) ii.next();
			if ((courant.getAttributeValue("type")).equals("application/lom+xml")){
				metaLom=courant.getAttributeValue("href");
				return metaLom;
			}
		}
		return metaLom;
	}
	public boolean updatePackageFromTempFile(String packId) {
		fileNameHierarchy=retrieveFileHierarchy(packId)+".xml";
		fileName=fileNameHierarchy;
		//fileName = packId+".xml";
		file = new File(PackageFile.filePathTemporary
				+ fileName);
		
		//file = new File(PackageFile.filePathTemporary
		//		+ File.separator + fileName);
		if(file.delete()){
			logger.info("file "+fileName+" deleted");
		} 
		else{
			logger.warn("File "+fileName+" can't be deleted");
			return false;
		}
		fileTemp.renameTo(file);
		logger.info("filetemp: "+fileTemp.getName()+" renomme en "+fileName);
		//juste une verif:
		//File file2 = new File(PackageFile.filePathTemporary
		//		+ File.separator + fileName);
		File file2 = new File(PackageFile.filePathTemporary
				+ fileName);
		if(file.exists()){
			logger.info("file "+fileName+" existe et a une taille de "+file.length());
		} 
		else{
			logger.warn("File "+fileName+" doesn't exist...");
			return false;
		}
		logger.info("filetemp est maintenant:"+fileTemp);
		fileTemp.delete();
		logger.info("fichier temp effacé...");
		return true;
	}
	
	public boolean removeFile(String packId){
		//fileName = packId+".xml";
		fileNameHierarchy=retrieveFileHierarchy(packId);
		/*file = new File(PackageFile.filePathTemporary
				+ File.separator + fileName);*/
		file = new File(PackageFile.filePathTemporary+
				fileNameHierarchy+".xml");
		if(file.delete()){
			logger.info("file "+fileNameHierarchy+" deleted");
			return true;
		} 
		else{
			logger.warn("File "+fileNameHierarchy+" can't be deleted");
			return false;
		}
	}
	
	public static String retrieveFileNameHierarchy(File file){
		//donne le nom du pack à partir du nom de fichier complet avec la hierachie. Ex: /a/2/4/+fin uuid
		String pathComplete="";
		try{
			pathComplete=file.getCanonicalPath();
		}catch(IOException e){
			logger.error("Impossible de trouver le fichier "+file);
			e.printStackTrace();
		}
		String hierarchy=pathComplete.substring(filePathTemporary.length());
		return hierarchy;
	}
	public static String retrievePackId(File file){
		//donne le nom du pack à partir du nom de fichier. En fait c'est comme l'uuid mais avec l'extension .xml
		String pathComplete="";
		try{
			pathComplete=file.getCanonicalPath();
		}catch(IOException e){
			logger.error("Impossible de trouver le fichier "+file);
			e.printStackTrace();
		}
		String packId=pathComplete.substring(filePathTemporary.length());
		packId=packId.replaceAll("/", "");
		return packId;
	}
	public static String retrieveFileHierarchy(String packId){
		String p=FileUtils.getFilePathHierarchy("", packId);
		return p;
	}
}
