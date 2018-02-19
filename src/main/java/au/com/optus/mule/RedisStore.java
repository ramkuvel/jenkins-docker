package au.com.optus.mule;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.context.MuleContextAware;
import org.mule.api.registry.RegistrationException;
import org.mule.api.store.ObjectStore;
import org.mule.api.store.ObjectStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;

public class RedisStore <T extends Serializable> implements ObjectStore<T>, MuleContextAware {

	@Autowired
	private RedisCacheManager cacheManager;
	private org.springframework.data.redis.cache.RedisCache cache;
	private MuleContext context;
	private DefaultMuleMessage message;
	
	@Autowired
	@Qualifier("redisCache")
	private int cacheAge;

	@Autowired
	public void setCache() {
		
		if(this.cacheAge >0){
			System.out.println("this.timeToLive : " + this.cacheAge);
			this.cacheManager.setDefaultExpiration(this.cacheAge);
		}
		
		this.cache = (RedisCache) this.cacheManager.getCache("TOKEN");
	}
	
	public RedisCache getCache() {
		return this.cache;
	}

	@Override
	public synchronized boolean contains(Serializable key) throws ObjectStoreException {
		System.out.println("Contains():::");
		if (cache.get(key.toString(), Object.class) == null)
			return false;
		else
			return true;
	}

	@Override
	public synchronized void store(Serializable key, T value) throws ObjectStoreException {

		System.out.println("Store():::");
		MuleEvent event = (MuleEvent) value;
		@SuppressWarnings("unchecked")
		List<Object> data = (List<Object>) event.getMessage().getPayload();
		this.cache.put(key.toString(), data);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized T retrieve(Serializable key) throws ObjectStoreException {

		System.out.println("Retrieve():::");
		List<Object> token = (List<Object>) cache.get(key.toString(), Object.class);
		
		DefaultMuleEvent event = null;
		if(token != null && token.size() > 0){
			message = new DefaultMuleMessage(token, context);
			FlowConstruct flow = context.getRegistry().lookupFlowConstruct("OAuthCache");
			event = new DefaultMuleEvent(message, org.mule.MessageExchangePattern.ONE_WAY, flow);
			return (T) event;
		}
		else{
			
			return null;
		}
	}
	
	public synchronized T remove(Serializable key) throws ObjectStoreException {
		
		System.out.println("Remove():::");
		T value = retrieve(key);
		
		if (value != null) {
			cache.evict(key);
		try {
		context.getRegistry().registerObject("evict", "Key " + key.toString() + " evicted from cache");
		} catch (RegistrationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		} else {
		try {
		context.getRegistry().registerObject("evict", "Key " + key.toString() + " not found");
		} catch (RegistrationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		}
		return value;
	}

	@Override
	public boolean isPersistent() {
		return true;
	}
	
	@Override
	public synchronized void clear() throws ObjectStoreException {
		System.out.println("Clear():::");
	}
	
	@Override
	public void setMuleContext(MuleContext context) {
		this.context = context;
	}
}