package com.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mobilestore.util.BeanUtil;
/**
 * Annotation to ignore model to bean conversion. This annotation used in bean {@link BeanUtil}.
 * @author shiv_2
 *
 */
@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreBeanConversion
{
	boolean ignore() default true;
}
