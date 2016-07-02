package com.diagramsf;

import android.support.annotation.IntDef;

import android.support.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * domain layer 入口，所有对数据层进行操作的行为都需要通过这个来进入。
 * domainLayer起到把数据处理逻辑和app需求逻辑隔离开来，在这里 衔接数据处理逻辑和app需求业务逻辑。
 * <p/>
 * 这层是一个纯Java的模块，不包含任何Android依赖。
 * Created by Diagrams on 2016/6/27 11:31
 */
public abstract class UseCase<T extends UseCase.RequestValue
        , R extends UseCase.ResponseValue
        , E extends UseCase.ErrorValue> {
    public static final int HIGH = 3;
    public static final int NORMAL = 2;
    public static final int LOW = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HIGH, NORMAL, LOW})
    @interface Type {}

    private T requestValue;
    private Listener<R> listener;
    private ErrorListener<E> errorListener;
    private int priority = 1;
    private boolean isCancel;
    private boolean justRun;//是否只是用来修饰Runnable

    private Object tag;//取消请求用的

    public void setPriority(@Type int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setRequestValue(@Nullable T value) {
        this.requestValue = value;
    }

    public T getRequestValue() {
        return requestValue;
    }

    public void setListener(Listener<R> listener) {
        this.listener = listener;
    }

    public Listener<R> getListener() {
        return listener;
    }

    public void setErrorListener(ErrorListener<E> errorListener) {
        this.errorListener = errorListener;
    }

    public ErrorListener<E> getErrorListener() {
        return errorListener;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public void cancel() {
        isCancel = true;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void justRun(){
        justRun = true;
    }

    public boolean isJustRun(){
        return justRun;
    }

    public void run() {
        execute(requestValue);
    }

    public abstract void execute(T requestValue);

    /** 执行请求的参数 */
    public interface RequestValue {

    }

    /** 结果数据 */
    public interface ResponseValue {

    }

    /** 错误结果 */
    public interface ErrorValue {
        void setException(Exception e);
    }

    /** 结果回调接口 */
    public interface Listener<R extends ResponseValue> {
        void onSucceed(R response);
    }//class Callback end

    /** 错误结果回调 */
    public interface ErrorListener<E extends ErrorValue> {
        void onError(E error);
    }

}//class UseCase end
