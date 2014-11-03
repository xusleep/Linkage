package service.framework.route.consistent.hash;

public class HashFunction {
	
	public int hash(Object key){
		return key.hashCode();
	}
}
