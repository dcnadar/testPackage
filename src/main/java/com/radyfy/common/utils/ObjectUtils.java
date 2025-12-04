package com.radyfy.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ObjectUtils {
    private static final Logger logger = LoggerFactory.getLogger(ObjectUtils.class);

    // get value of field in a object
    public static Object getFormFieldValue(String field, Object o) {
        try {
            String[] fields;
            if (field.contains(".")) {
                fields = field.split("\\.");
            } else {
                fields = new String[]{field};
            }

            for (int i = 0; i < fields.length && o != null; i++) {
                String name = fields[i];

//                String pre = "get";
//                boolean superClass = false;

//                try {
//                    o.getClass().getDeclaredField(name).isAnnotationPresent(FormItem.class);
//                } catch (NoSuchFieldException e) {
//                    superClass = true;
//                }

                String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);

                Method method = getMethod(o.getClass(), methodName);
//                if (superClass) {
//                    method = o.getClass().getSuperclass().getDeclaredMethod(methodName);
//                } else {
//                    method = o.getClass().getDeclaredMethod(methodName);
//                }
                if(method != null) {
                    Object value = method.invoke(o);
                    if (i == fields.length - 1) {
                        return value;
                    }
                    o = value;
                } else {
                    return null;
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error("Failed to get form field value: " + e.getMessage(), e);
        }
        return null;
    }

    private static Method getMethod(Class<?> klass, String name, Class<?>... parameterTypes){
        try {
            return klass.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            klass = klass.getSuperclass();
            if(klass == null || Object.class.equals(klass)){
                return null;
            }
            return getMethod(klass, name, parameterTypes);
        }
    }

    public static Field getField(Class<?> klass, String name){
        try {
            return klass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            klass = klass.getSuperclass();
            if(klass == null || Object.class.equals(klass)){
                return null;
            }
            return getField(klass, name);
        }
    }

    // set value of field in a object
    public static void setFormFieldValue(String field, Object value, Object o) {
        String[] fields;
        try {
            if (field.contains(".")) {
                fields = field.split("\\.");
            } else {
                fields = new String[]{field};
            }

            for (int i = 0; i < fields.length && o != null; i++) {
                String name = fields[i];
                String fieldName = name.substring(0, 1).toUpperCase() + name.substring(1);

                if (i == fields.length - 1) {
                    String methodName = "set" + fieldName;
                    Method method = getMethod(o.getClass(), methodName, value.getClass());

//                    try {
//                        if (o.getClass().getDeclaredField(name).isAnnotationPresent(FormItem.class)) {
//                            method = o.getClass().getDeclaredMethod(methodName, value.getClass());
//                        }
//                    } catch (NoSuchFieldException e) {
//                        method = o.getClass().getSuperclass().getDeclaredMethod(methodName, value.getClass());
//                    }

                    if (method != null) {
                        method.invoke(o, value);
                    }
                } else {
                    String methodName = "get" + fieldName;

                    Method method = getMethod(o.getClass(), methodName);
                    Object o1 = method.invoke(o);
                    if (o1 == null) {
                        Class<?> type = o.getClass().getDeclaredField(name).getType();
                        o1 = type.newInstance();
                        String setMethodName = "set" + fieldName;
                        method = getMethod(o.getClass(), setMethodName, type);
                        method.invoke(o, o1);
                    }
                    o = o1;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
