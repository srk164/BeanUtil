package com.mobilestore.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import com.common.annotation.IgnoreBeanConversion;
import com.common.annotation.IgnoreModelConversion;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilestore.bean.interfaces.CommonBean;
import com.mobilestore.exception.BeanConversionException;
import com.view.common.View;




public class BeanUtil {
	
	private static final Map<String, String[]> IGNORE_PROPERTIES_MAP = createMap();
	private static final Logger logger = Logger.getLogger(BeanUtil.class);
    private static Map<String, String[]> createMap() {
        Map<String, String[]> result = new HashMap<String, String[]>();
        String billingInfo[] = {"components"};
        String user[] = {"password"};
        result.put("BillingInfo", billingInfo);
        result.put("User", user);
        return Collections.unmodifiableMap(result);
    }
	
	public static void copyProperties(Object source, Object target)
	{
//		Converter converter = new DateConverter(null);
//		BeanUtilsBean beanUtilsBean = BeanUtilsBean.getInstance();
//		beanUtilsBean.getConvertUtils().register(converter, Date.class);    
		// This method can be used directly but to avoid modifying controller code , if other api model used to do same job.
		BeanUtils.copyProperties(source, target);
	}
	
	public static void copyProperties(Object source, Object target ,boolean ignoreProperties)
	{
		String className =  source.getClass().getName();
		if (! ignoreProperties || !IGNORE_PROPERTIES_MAP.containsKey(className)) 
		{
			BeanUtils.copyProperties(source, target);
		}
		else 
		{
			
			BeanUtils.copyProperties(source, target, IGNORE_PROPERTIES_MAP.get(className));
		}
	}
	
	
	/**
	 *  Use {@link #prepareListofObjects(List<S>,Class<T>,Set)} if you need to neglect few properties.
	 */
	public static <T, S> List<T> prepareListofObjects(Collection<S> sources,Class<T> targetClass) throws BeanConversionException 
	{
		return prepareListofObjects(sources, targetClass, null);
	}

	public static <T, S> List<T> prepareListofObjects(Collection<S> sources,Class<T> targetClass, Set<String> neglectables) throws BeanConversionException 
	{
		List<T> targets = null;
		try {
			if(sources != null && !sources.isEmpty())
			{
				targets = new ArrayList<T>();
				T target = null;
				for(S source : sources)
				{
					if ( checkParentInterface(targetClass, CommonBean.class))
					{
						//If bean instance to be created.
						target = (T) prepareBeanInstanceByClass(source, targetClass, neglectables);
					}
					else
					{
						//If model instance to be created.
						target  = (T) prepareModelInstanceByClass(source, targetClass, true);
					}
					targets.add(target);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new BeanConversionException(" prepareListofObjects !! targetClass: "+targetClass.getName() );
		}
		return targets;
	}
	

	
	public static <T, S> Set<T> prepareSetofObjects(Collection<S> sources,Class<T> targetClass, Set<String> neglectables) throws BeanConversionException 
	{
		Set<T> targets = null;
		try {
			if(sources != null && !sources.isEmpty())
			{
				targets = new HashSet<T>();
				T target = null;
				for(S source : sources)
				{
					if ( checkParentInterface(targetClass, CommonBean.class))
					{
						//If bean instance to be created.
						target = (T) prepareBeanInstanceByClass(source, targetClass, neglectables);
					}
					else
					{
						//If model instance to be created.
						target  = (T) prepareModelInstanceByClass(source, targetClass, true);
					}
					targets.add(target);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new BeanConversionException(" prepareSetofObjects !! targetClass: "+targetClass.getName() );
		}
		return targets;
	}
	
	//TODO: causing JVM crash. if the inner model contains List. 
	
	public static Object prepareInstance(Object modelObj, Class beanClass, boolean ignoreProp) throws BeanConversionException 
	{
		Object bean = null;
		
			try {
				bean = beanClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
				throw new BeanConversionException(" prepareInstance InstantiationException !! beanClass: "+beanClass.getName() );
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new BeanConversionException(" prepareInstance IllegalAccessException !! beanClass: "+beanClass.getName() );
			}
			copyProperties(modelObj, bean ,ignoreProp);
		
		return bean;
	}
	/**
	 * Use {@link #prepareBeanInstanceByClass(Object,Class,Set)} to ignore some properties while converting.
	 */
	public static Object prepareBeanInstanceByClass(Object modelObj, Class beanClass) throws BeanConversionException 
	{
		if (modelObj == null) {
			return null;
		}
		return prepareBeanInstanceByClass(modelObj, beanClass, null);
	}

	public static Object prepareBeanInstanceByClass(Object modelObj, Class beanClass, Set<String> neglectables) throws BeanConversionException 
	{
		Object beanObject = prepareNoPassowrdBean(modelObj, beanClass);
		return prepareBeanInstance(modelObj,  beanObject, neglectables);
	}
	
	public static Object prepareNoPassowrdBean(Object modelObj, Class beanClass) throws BeanConversionException
	{
		
		Object bean = null;
		try {
			bean = beanClass.newInstance();
			String password[] = {"password"};
			BeanUtils.copyProperties(modelObj, bean, password);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new BeanConversionException(" prepareNoPassowrdBean !! beanClass: "+beanClass.getName() );
		}
		
		return bean;
	}
	
	/**
	 * @param modelObj
	 * @param isComplex
	 * @param beanObject
	 * @return
	 * @throws BeanConversionException 
	 * @deprecated Use {@link #prepareBeanInstance(Object,Object,Set)} instead
	 */
	private static Object prepareBeanInstance(Object modelObj, Object beanObject) throws BeanConversionException {
		return prepareBeanInstance(modelObj, beanObject, null);
	}

	/**
	 * @param modelObj
	 * @param beanObject
	 * @param neglectables TODO
	 * @param isComplex
	 * @return
	 * @throws BeanConversionException 
	 */
	private static Object prepareBeanInstance(Object modelObj, Object beanObject, Set<String> neglectables) throws BeanConversionException {
			try {
				Class beanClass = beanObject.getClass();
				Field[] fields = beanClass.getDeclaredFields();
				for (Field field : fields) 
				{
					Class<?> c = field.getType();
					String propertyName  = field.getName();
					
					if ( field.isAnnotationPresent(IgnoreBeanConversion.class) 
							|| (neglectables!=null && neglectables.contains(propertyName)))
					{
						
						//TODO: temp fix to reset the properties of ignored collections.
						if (c.equals(Set.class) ) 
						{
							try { 
								Method modelSETMethod = beanObject.getClass().getMethod(getSetterMethodName(propertyName), Set.class);
								Set beanSet = new HashSet();
								modelSETMethod.invoke(beanObject, beanSet );
							} catch (NoSuchMethodException e) {
								//Do nothing as this is in ignore if block.
								
							}
						}
						else if (c.equals(List.class) ) 
						{
							try {
								Method modelSETMethod = beanObject.getClass().getMethod(getSetterMethodName(propertyName), Set.class);
								List beanSet = new ArrayList();
								modelSETMethod.invoke(beanObject, beanSet );
							} catch (NoSuchMethodException e) {
								//Do nothing as this is in ignore if block.
							}
						}
						
						continue;
					}
					
					
					Method beanGETMethod = modelObj.getClass().getMethod(getGetterMethodName(propertyName));
					Object innerModel = beanGETMethod.invoke(modelObj );
					if (innerModel == null )
						continue;
					
					if( c.getSuperclass() != null && c.getSuperclass() != null && c.getSuperclass().getName() != null 
							&& checkParentInterface(c, CommonBean.class) )
					{
						//propertyName = propertyName.substring(0, propertyName.length()-4); // Model property name should be same.
						
						Class innermBeanClass = beanObject.getClass().getDeclaredField(propertyName).getType();
						Object innerBean = prepareBeanInstanceByClass(innerModel, innermBeanClass, neglectables);
						Method method = beanObject.getClass().getMethod(getSetterMethodName(propertyName), innerBean.getClass());
						method.invoke(beanObject,innerBean );
					}
					else if (c.equals(Set.class) || c.equals(List.class) )
					{
						Type genericClassType = beanObject.getClass().getDeclaredField(propertyName).getGenericType();
						Class innerBeanClass = getInnerCollectionBeanClassType(genericClassType);
						
						
						// For normal properties, set should be loaded when required.
						Method modelGETMethod = modelObj.getClass().getMethod(getGetterMethodName(propertyName));
						Collection innerSetModel = (Collection) modelGETMethod.invoke(modelObj );
						if (innerSetModel == null)
						{
							continue;
						}
						if (c.equals(Set.class) )
						{
							Set beanSet = new HashSet();
							for (Object modelObject : innerSetModel)
							{
								Object innerBean = prepareBeanInstanceByClass(modelObject, innerBeanClass, neglectables);
								beanSet.add(innerBean);
							}
							Method modelSETMethod = beanObject.getClass().getMethod(getSetterMethodName(propertyName), Set.class);
							modelSETMethod.invoke(beanObject,beanSet );
						}else if (c.equals(List.class))
						{
							List beanSet = new ArrayList();
							for (Object modelObject : innerSetModel)
							{
								Object innerBean = prepareBeanInstanceByClass(modelObject, innerBeanClass, neglectables);
								beanSet.add(innerBean);
							}
							Method modelSETMethod = beanObject.getClass().getMethod(getSetterMethodName(propertyName), List.class);
							modelSETMethod.invoke(beanObject,beanSet );
						}
						
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
				throw new BeanConversionException(" Exception !! beanObject: "+beanObject.getClass().getName() , e);
				
			} 
			
		return beanObject;
	}

	/**
	 * @param genericCollectionClassType
	 * @return
	 */
	public static Class getInnerCollectionBeanClassType(Type genericCollectionClassType) {
		Class innerBeanClass = null;
		if(genericCollectionClassType instanceof ParameterizedType)
		{
		    ParameterizedType type = (ParameterizedType) genericCollectionClassType;
		    Type[] typeArguments = type.getActualTypeArguments();
		    for(Type typeArgument : typeArguments)
		    {
		    	innerBeanClass = (Class) typeArgument;
		        break;
		    }
		}
		return innerBeanClass;
	}
	/**
	 * @param propertyName
	 * @return
	 */
	public static String getGetterMethodName(String propertyName) {
		return "get"+toFirstLetterUpper(propertyName);
	}
	
	
	public static String toFirstLetterUpper(String propertyName) {
		return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
	}
	
	public static String getSetterMethodName(String propertyName) {
		return "set"+toFirstLetterUpper(propertyName);
	}
	
	public static Object prepareModelInstanceByClass(Object beanObj, Class modelClass, boolean createTenant) throws BeanConversionException 
	{
		Object modelObject = prepareInstance(beanObj, modelClass, false);
		return prepareModeInstance(beanObj, modelObject, createTenant);
	}
	
	
	/**
	 * @param beanObj
	 * @param modelObject
	 * @param createTenant - it will get the tenant from session and set on the object.
	 * @param isComplex
	 * @return 
	 * @throws BeanConversionException 
	 */
	private static Object prepareModeInstance(Object beanObj,
			Object modelObject, boolean createTenant) throws BeanConversionException 
	{
		try 
		{
			Field[] fields = beanObj.getClass().getDeclaredFields();

			copyProperties(beanObj, modelObject);
			for (Field field : fields) 
			{
				Class<?> c = field.getType();
				String propertyName  = field.getName();
				if ( field.isAnnotationPresent(IgnoreModelConversion.class))
				{
					continue;
				}
				
				try 
				{
					if( !c.equals(Set.class) && !c.equals(List.class) && c.getSuperclass() != null && c.getSuperclass().getName() != null 
							&& checkParentInterface(c, CommonBean.class))
					{
						
							Method beanGETMethod = beanObj.getClass().getMethod(getGetterMethodName(propertyName));
							Object innerBean = beanGETMethod.invoke(beanObj );
							if (innerBean == null)
								continue;
							Class innermModelClass = modelObject.getClass().getDeclaredField(propertyName).getType();
							Object innerModel = prepareModelInstanceByClass(innerBean, innermModelClass, createTenant );

							Method modelSETMethod = modelObject.getClass().getMethod(getSetterMethodName(propertyName), innermModelClass);
							modelSETMethod.invoke(modelObject,innerModel );
						
						
					}
					else if (c.equals(Set.class) || c.equals(List.class) )
					{
						Type innermModelClass = modelObject.getClass().getDeclaredField(propertyName).getGenericType();
						Class innerModelClass = getInnerCollectionBeanClassType(innermModelClass);


						// For normal properties, set should be loaded when required.
						Method beanGETMethod = beanObj.getClass().getMethod(getGetterMethodName(propertyName));
						Collection innerSetBean = (Collection) beanGETMethod.invoke(beanObj );
						if (innerSetBean == null)
						{
							continue;
						}
						
						if (c.equals(Set.class) )
						{
							Set modelSet = new HashSet();
							for (Object beanObject : innerSetBean)
							{
								Object innerModel = prepareModelInstanceByClass(beanObject, innerModelClass, createTenant );
								modelSet.add(innerModel);
							}
							Method modelSETMethod = modelObject.getClass().getMethod(getSetterMethodName(propertyName), Set.class);
							modelSETMethod.invoke(modelObject,modelSet );
						} else if (c.equals(List.class))
						{
							List modelSet = new ArrayList();
							for (Object beanObject : innerSetBean)
							{
								Object innerModel = prepareModelInstanceByClass(beanObject, innerModelClass, createTenant );
								modelSet.add(innerModel);
							}
							Method modelSETMethod = modelObject.getClass().getMethod(getSetterMethodName(propertyName), List.class);
							modelSETMethod.invoke(modelObject,modelSet );
						}
						
					}
				} catch (Throwable e) {
					throw new BeanConversionException(" Exception !! propertyName: "+propertyName +" Class: "+c , e );
				} 
			}

			return modelObject;
		} catch (Throwable e)
		{
			e.printStackTrace();
			throw new BeanConversionException("Exception in prepareModeInstance method ", e );
		}
	}
	
	
	public static <T> T convertJsonToObject(String json , Class<T> t) throws JsonParseException, JsonMappingException, IOException 
	{
		ObjectMapper mapper = new ObjectMapper();		
		return mapper.readValue(json, t);
	}
	
	public static String convertObjectToJson(Object object) throws JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();	
		return mapper.writeValueAsString(object);
	}
	
	/**
	 * Method checks if the target class is implementing parent interface by package name.
	 * @param targetClass
	 * @param parentInterfacePackage
	 * @return
	 */
	public static boolean checkParentInterface(Class targetClass,Class parentInterfaceClass)
	{
		return parentInterfaceClass.isAssignableFrom(targetClass);
	}
	
	public static Class<?> getClassForKey(Class entityClass, String key) throws NoSuchFieldException, SecurityException
	{
		String[] keys = key.split("\\.",2);
		Field field = entityClass.getDeclaredField(keys[0]);
		
		if (keys.length > 1 ) {
			// Invoking recursively to get the end property type eg. for key subCategory.Name
			// first iteration will split once and get name in index 1, and upon recursive call 
			// in next iteration we will get the type of name that is string.
			
			Class<?> c = field.getType();
			if (BeanUtil.checkParentInterface(c , Collection.class))
			{
				Type genericType = field.getGenericType();
				// If the elements are in collection then get the element type.
				c = BeanUtil.getInnerCollectionBeanClassType(genericType);
			}
			return getClassForKey(c,keys[1]);
		} else 
		{
			return field.getType();
		}
		
	}

	public static <S,T> List<T> prepareListofViews(Collection<S> sources,
			Class<T> targetClass) throws BeanConversionException 
	{
		List<T> targets = null;
		try
		{
			if(sources != null && !sources.isEmpty())
			{
				targets = new ArrayList<T>();
				for(S source : sources)
				{
					View view = (View) targetClass.newInstance();
					view.copyProperties(source);
					targets.add((T) view);
				}
			}
		} catch (Throwable e) 
		{
			logger.error(StringUtil.getErrorWithTimenLogin("Could not convert to list of views for " + targetClass.getName()),e);
			throw new BeanConversionException(" Exception while converting view :  "+targetClass.getName(),e );
		}
		return targets;
	}
}
