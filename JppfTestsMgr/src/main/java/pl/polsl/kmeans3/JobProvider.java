package pl.polsl.kmeans3;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.linear.RealVector;
import org.jppf.JPPFException;
import org.jppf.client.JPPFJob;

import pl.polsl.data.beans.CacheEntry;

public class JobProvider {
	
	private AtomicInteger submittedTasks;
	private AtomicInteger finishedTasks;
	
	private ClosestCentroidMerger closestCentroidsMerger;
	private NewCentroidsMerger newCentroidsMerger;
	
	
	public JobProvider(){
		this.submittedTasks = new AtomicInteger();
		this.finishedTasks = new AtomicInteger();
	}
	
	public JPPFJob createClosestCentroidsJob(List<CacheEntry<RealVector>> data, List<RealVector> centroids) throws JPPFException{
		submittedTasks.set(0);
		finishedTasks.set(0);
		JPPFJob job = new JPPFJob();
    	job.setName("closestCentroids");
    	this.closestCentroidsMerger = new ClosestCentroidMerger(finishedTasks);
    	job.addJobListener(this.closestCentroidsMerger);
    	for(CacheEntry<RealVector> vectors: data){
    		job.add(new ClosestCentroidAllocationTask(vectors.getKey(), vectors.getVectors(), centroids), null);
    		submittedTasks.addAndGet(1);
    	}
    	
    	return job;
	}
	
	public List<JPPFJob> createClosestCentroidsJobs(List<CacheEntry<RealVector>> data, List<RealVector> centroids) throws JPPFException{
		submittedTasks.set(0);
		finishedTasks.set(0);
		
		List<JPPFJob> result = new LinkedList<JPPFJob>();
		int counter = 0;
		
		JPPFJob job = new JPPFJob();
		job.setBlocking(false);
		this.closestCentroidsMerger = new ClosestCentroidMerger(finishedTasks);
		job.addJobListener(this.closestCentroidsMerger);
		
		for(CacheEntry<RealVector> vectors: data){
			if(job == null){
				job = new JPPFJob();
				job.setBlocking(false);
				job.addJobListener(this.closestCentroidsMerger);
			}
			
			job.add(new ClosestCentroidAllocationTask(vectors.getKey(), vectors.getVectors(), centroids), null);
    		submittedTasks.addAndGet(1);
    		counter++;
		}
		
		if(job != null)
			result.add(job);
		
		return result;
	}
	
	public JPPFJob createGroupByAndAverageJob(Collection<? extends Pair<Integer, List<RealVector>>> pairs) throws JPPFException{
		submittedTasks.set(0);
		finishedTasks.set(0);
		JPPFJob job = new JPPFJob();
    	job.setName("groupByAndAverage");
    	this.newCentroidsMerger = new NewCentroidsMerger(finishedTasks);
    	job.addJobListener(this.newCentroidsMerger);
    	for(Pair<Integer, List<RealVector>> pair: pairs){
    		job.add(new NewCentroidsTask(pair), null);
    		submittedTasks.addAndGet(1);
    	}
    	
    	return job;
	}
	
	public List<JPPFJob> createGroupByAndAverageJobs(Collection<? extends Pair<Integer, List<RealVector>>> pairs, int maxTasksPerJob) throws JPPFException{
		submittedTasks.set(0);
		finishedTasks.set(0);
		
		List<JPPFJob> result = new LinkedList<JPPFJob>();
		this.newCentroidsMerger = new NewCentroidsMerger(finishedTasks);
		int counter = 0;
		JPPFJob job = new JPPFJob();
		job.setBlocking(false);
		job.addJobListener(this.newCentroidsMerger);
		for(Pair<Integer, List<RealVector>> pair: pairs){
			if(job == null){
				job = new JPPFJob();
				job.setBlocking(false);
				job.addJobListener(this.newCentroidsMerger);
			}
			
			job.add(new NewCentroidsTask(pair), null);
    		submittedTasks.addAndGet(1);
    		counter++;
    		
    		if(counter >= maxTasksPerJob){
    			counter = 0;
    			result.add(job);
    			job = null;
    		}
		}
		
		if(job != null)
			result.add(job);
				
		return result;
	}
	
	public int getProcessedTasksCount(){
		return this.finishedTasks.get();
	}
	
	public int getSubmittedTasksCount(){
		return this.submittedTasks.get();
	}

	public ClosestCentroidMerger getClosestCentroidsMerger() {
		return closestCentroidsMerger;
	}

	public void setClosestCentroidsMerger(
			ClosestCentroidMerger closestCentroidsMerger) {
		this.closestCentroidsMerger = closestCentroidsMerger;
	}

	public NewCentroidsMerger getNewCentroidsMerger() {
		return newCentroidsMerger;
	}

	public void setNewCentroidsMerger(NewCentroidsMerger newCentroidsMerger) {
		this.newCentroidsMerger = newCentroidsMerger;
	}

}
