package realnot.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;


public class JArchiveLog {

    protected final static Logger _logger = Logger.getLogger(JArchiveLog.class);

	private static boolean	_drymode = true;
	private static String	_configFile = "";
	private static String  	_pattern = ""; 
	private static String  	_directory = ""; 
	private static String	_dateFormat="";
	private static Matcher 	_matcher;
	private static String	_archiveDir = "";
	private static Boolean	_purgeafter = false;

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		
		DoParseOptions(args);
		ReadProp();
		DoArchive(_directory);

		_logger.info("OK");

		System.exit(0);		
	}

	private static void ReadProp()
	{

		Properties prop = new Properties();
		InputStream input = null;

		try 
		{
			input = new FileInputStream(_configFile);

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			_pattern = prop.getProperty("pattern");
			_directory = prop.getProperty("directory");			
			_dateFormat = prop.getProperty("dateformat");
			_archiveDir = prop.getProperty("archivedir");						

			_logger.info("Pattern : " + _pattern);
			_logger.info("Directory : " + _directory);
			_logger.info("Archive dir : " + _archiveDir);
			_logger.info("Format Date : " + _dateFormat);


		} 
		catch (Exception ex) 
		{
			_logger.error(ex.getMessage());
			System.exit(1);
		} 
		finally 
		{
			if (input != null) 
			{
				try {
					input.close();
				} catch (IOException e) 
				{
					_logger.error(e.getMessage());
					System.exit(2);
				}
			}
		}
		
	}
	
	private static void DoParseOptions(String[] args)
	{
		// create Options object
		Options options = new Options();
		
		options.addOption("p", false, "Purge");
		
		Option  Omode = Option.builder("m")
						.hasArg()
						.longOpt("mode")
						.required()
						.argName("mode")
						.build();
		
		options.addOption(Omode);

		Option  Oconf = Option.builder("c")
				.hasArg()
				.longOpt("config")
				.required()
				.argName("config")
				.build();

		options.addOption(Oconf);
		
		CommandLineParser parser = new DefaultParser();
		
		try 
		{
			CommandLine cmd = parser.parse( options, args);
						
			String mode = cmd.getOptionValue("mode");

			if(mode.toLowerCase().equals("exec"))
			{
				_logger.info("mode exec");
				_drymode = false;
			}
			else
				if(mode.toLowerCase().equals("dry"))
				{
					_logger.info("mode dry");
					_drymode = true;
				}
				else
				{
					_logger.error("mode s'execution inconnu : " + mode);
					System.exit(1);
				}
			
			_configFile = cmd.getOptionValue("config");
			_logger.info("Config : " + _configFile);
			
			
			if(cmd.hasOption('p'))
			{
				_purgeafter = true;
			}
			
			_logger.info("Purge : " + (_purgeafter ? "oui" : "non"));
			
		} catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			_logger.error(e.getMessage());
			System.exit(1);
		}						
	}
	
	public static void AddToZipFile(String fileName, String zipfilename) throws FileNotFoundException, IOException 
	{
		if(! _drymode)
		{
			_logger.debug("Writing '" + fileName + "' to zip file");
			
			FileOutputStream fos = new FileOutputStream(zipfilename);
			ZipOutputStream zos = new ZipOutputStream(fos);
			
			File file = new File(fileName);
			FileInputStream fis = new FileInputStream(file);
	
			_logger.debug("Adding '" + file.getName());
	
			ZipEntry zipEntry = new ZipEntry(file.getName());
			zos.putNextEntry(zipEntry);
	
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) 
			{
				_logger.debug("readed " + length + " byte(s) from " + file.getName());			
				zos.write(bytes, 0, length);
			}
	
			zos.closeEntry();
			
			zos.close();		
			fos.close();
			fis.close();
		}
		else
		{
			_logger.info("Writing '" + fileName + "' to " + zipfilename);			
		}
	}

	private static void CreateArchiveDir()
	{
		File theDir = new File(_archiveDir);
		theDir.mkdir();
	}
	
	private static void DoArchive(String directory)
	{
		_logger.debug("Start archive : " + directory);
		
		CreateArchiveDir();
		
		Pattern pattern = Pattern.compile(_pattern);
		
		// check source dir exist
		
		File rep = null;
		
		try
		{
			rep = new File(directory);
		}
		catch(NullPointerException e)
		{
			_logger.error("Bad directory : " + directory);
			System.exit(1);
		}
		
		if(!rep.exists())
		{
			_logger.error("Bad directory : " + directory);
			System.exit(1);			
		}
		
		
		File[] fichiers = rep.listFiles(new FilenameFilter() 
		{
		  public boolean accept(File dir, String name) 
		  {
			  _matcher = pattern.matcher(name);
			  _logger.debug("checking : " + name);
		    return _matcher.find();
		  }
		});				
		
		SimpleDateFormat formatter = new SimpleDateFormat(_dateFormat);

		Calendar c = Calendar.getInstance ();
		
		for(int i = 0; i < fichiers.length; i++)
		{
			_logger.debug("fichier match : " + fichiers[i].getName());
			// Extraction de la date
			if(fichiers[i].isDirectory())
			{
				_logger.debug(fichiers[i].getName() + " match mais c'est un répertoire");
				continue;  
			}
			  
			Date today = c.getTime ();
			String date = formatter.format(today);
			
			_logger.debug("date " + date);
			
			String zipFileName = _archiveDir + "\\" + fichiers[i].getName() + "-" + date + ".zip";
			
			_logger.debug("zip file " + zipFileName);
						
			try 
			{
				AddToZipFile(fichiers[i].getAbsolutePath() , zipFileName);
			} 
			catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				_logger.error("FileNotFoundException : " + e.getMessage());
				_logger.error(e);
				System.exit(1);
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				_logger.error("IOException : " + e.getMessage());
				_logger.error(e);
				System.exit(1);
			}
			
			if(_purgeafter && !_drymode)
			{
				_logger.debug("Purge de " + fichiers[i].getName());
				fichiers[i].delete();
			}
			  
		}				
		
	} // DoArchive
	
}
