package com.mopub.mobileads.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Reflection {

    public static class MethodBuilder {
        private final Object mInstance;
        private final String mMethodName;
        private final Class<?> mClass;

        private List<Class<?>> mParameterClasses;
        private List<Object> mParameters;
        private boolean mIsAccessible;

        public MethodBuilder(final Object instance, final String methodName) {
            mInstance = instance;
            mMethodName = methodName;

            mParameterClasses = new ArrayList<Class<?>>();
            mParameters = new ArrayList<Object>();

            mClass = (instance != null) ? instance.getClass() : null;
        }

        public <T> MethodBuilder addParam(Class<T> clazz, T parameter) {
            mParameterClasses.add(clazz);
            mParameters.add(parameter);

            return this;
        }

        public MethodBuilder setAccessible() {
            mIsAccessible = true;

            return this;
        }

        public Object execute() throws Exception {
            Class<?>[] classArray = new Class<?>[mParameterClasses.size()];
            Class<?>[] parameterTypes = mParameterClasses.toArray(classArray);

            Method method = mClass.getDeclaredMethod(mMethodName, parameterTypes);

            if (mIsAccessible) {
                method.setAccessible(true);
            }

            Object[] parameters = mParameters.toArray();
            return method.invoke(mInstance, parameters);
        }

    }
}
