package pl.polsl.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pl.polsl.data.beans.CacheEntry;

public abstract class AbstractDataPreparator<T> {
	public abstract T createObjectFromString(String str);
	private File dictionaryFile;
	private FileReader fr;
	private BufferedReader br;
	
	public AbstractDataPreparator(String file){
		dictionaryFile = new File(file);
		try {
			fr = new FileReader(dictionaryFile);
			br = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void refreshDataSource() throws FileNotFoundException{
		fr = new FileReader(this.dictionaryFile);
		br = new BufferedReader(this.fr);
	}
	
	public List<T> getAllData(){
		return getAllData(1).get(0);
	}
	
	public List<List<T>> getAllData(int parts){
		List<List<T>> finalResult = new LinkedList<>();
		
		for(int i = 0; i < parts; i++){
			finalResult.add(new LinkedList<T>());
		}
		
		if(br != null){
			String line;
			try {
				int counter = 0;
				while((line = br.readLine()) != null){
					finalResult.get(counter % parts).add(createObjectFromString(line));
					counter++;
				}
			} catch (IOException e) {
				e.printStackTrace();
				finalResult=null;
			}
			finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if(fr != null){
					try {
						fr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			return finalResult;
		}
		else
			return null;
	}
	
	public List<CacheEntry<T>> getAllDataCacheEntry(int parts){
		return wrapWithCacheEntry(getAllData(parts));
	}
	
	private List<CacheEntry<T>> wrapWithCacheEntry(List<List<T>> allData) {
		List<CacheEntry<T>> result = new LinkedList<>();
		for(int i=0; i < allData.size(); i++){
			CacheEntry<T> entry = new CacheEntry<T>();
			entry.setKey(i);
			entry.setVectors(allData.get(i));
			result.add(entry);
		}
		
		return result;
	}

	public List<List<T>> getPartitionedData(int partitionSize){
		if(br != null && partitionSize > 0){
			List<List<T>> result = new ArrayList<>();
			String line;
			try {
				List<T> embeddedList = new ArrayList<>();
				int counter = 0;
				while((line = br.readLine()) != null){
					embeddedList.add(createObjectFromString(line));
					counter++;
					if(counter == partitionSize){
						result.add(embeddedList);
						embeddedList = new ArrayList<>();
						counter = 0;
					}
				}
				if(embeddedList != null && embeddedList.size() > 0){
					result.add(embeddedList);
				}
			} catch (IOException e) {
				e.printStackTrace();
				result=null;
			}
			finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(fr != null){
					try {
						fr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			return result;
		}
		else
			return null;	
	}
}
