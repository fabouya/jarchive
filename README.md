# jarchive
archivage de log en java

# Usage:

java -jar jarchive.jar -c \<config file\> -m [dry|exec] [-p]

## Options
* **-c, --config :**  config file
* **-m, --mode :**  mode, **dry** mode just show what will be purged, **exec** mode do the zip
* **-p :**  purge mode, clean file after archive (only if exec mode)
      
      
## Config file

The configuration file is java's properties file type.  
Values :  

* **pattern :** this is a regex containing the pattern to filter files to archive  
* **dateformat :** this the format we'll use to append to the filename to form archive's name  
(see https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html for date reference)    
* **directory :** the directory to archive
* **archivedir :** the target directory to store zip files
    
  Note : character \ should be doubled in the pattern and the directory    
    
### Example :  

**config file content : (config.properties)**    
pattern=.+log.*  
dateformat=yyyyMMddHHmmss  			(will create files named : <source file name>-yyyyMMddHHmmss.zip
directory=C:\\test_fl\\logs  
archivedir=C:\\test_fl\\logs\\archives  

**command :**  
java -jar jarchive.jar -c config.properties -m exec -p

=> will create zipped files from C:\test_fl\logs to  C:\\test_fl\\logs\\archives, and erase sources files
