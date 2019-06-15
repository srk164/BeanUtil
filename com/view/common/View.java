package com.view.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mobilestore.exception.BeanConversionException;
import com.mobilestore.util.BeanUtil;

public abstract class View 
{

	
	/**
	 * @return the metaData
	 */
	public abstract Map<String, String> getMetaData();
	
	/**
	 * @param modelObj
	 * @return
	 * @throws BeanConversionException 
	 */
	public void copyProperties(Object modelObj) throws BeanConversionException
	{
		try {
			Map<String, String> map = this.getMetaData();
			Set<Entry<String, String>> entrySet = map.entrySet();
			for (Entry<String, String> entry : entrySet) 
			{
				String name = entry.getKey();
				String setterMethodName = BeanUtil.getSetterMethodName(name);

				//Fetch Value.
				String mapping = entry.getValue();

				Object value = getValue(mapping,modelObj);
				if (value != null) 
				{
					Method method =  null;
					if (value instanceof java.sql.Timestamp) 
					{
						method = this.getClass().getMethod(setterMethodName, Date.class);
					}
					else if (value instanceof Integer) 
					{
						method = this.getClass().getMethod(setterMethodName,int.class );
					}
					else if (value instanceof Boolean) 
					{
						method = this.getClass().getMethod(setterMethodName,boolean.class );
					}
					else {
						method = this.getClass().getMethod(setterMethodName, value.getClass());
					}
					method.invoke(this,value );
				}
			}
		} catch (Throwable e) 
		{
			throw new BeanConversionException(" Could not convert the class "+this.getClass().getName() , e);

		} 

	}
	
	
	private static Object getValue(String mapping, Object modelObj) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		String[] strarr  = mapping.split("\\.");
		if (strarr.length >1)
		{
			Object innerModel = modelObj;
			for (int j = 0; j < strarr.length-1; j++) 
			{
				// iterate to access the getter methods for complete path.
				String propertyName =  strarr[j];
				Method beanGETMethod = innerModel.getClass().getMethod(BeanUtil.getGetterMethodName(propertyName));
				innerModel = beanGETMethod.invoke(innerModel);
				if (innerModel == null) 
				{
					break;
				}
			}
			if (innerModel != null) 
			{
				//  Last node property.
				String propertyName =  strarr[strarr.length-1];
				Method beanGETMethod = innerModel.getClass().getMethod(BeanUtil.getGetterMethodName(propertyName));
				return beanGETMethod.invoke(innerModel );
			}
			
		}else 
		{
			String propertyName =  strarr[0];
			Method beanGETMethod = modelObj.getClass().getMethod(BeanUtil.getGetterMethodName(propertyName));
			return beanGETMethod.invoke(modelObj );
		}
		return null;
		
	}

} 