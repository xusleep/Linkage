package service.framework.route.consistent.hash;

public class HashFunction {
	
	public static int hash(Object key){
		return key.hashCode();
	}
}
