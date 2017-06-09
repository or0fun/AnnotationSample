package com.baiwanlu.processor;

import com.baiwanlu.annotation.MyAnnotation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(MyProcessor.class)
public class MyProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

//`Types`: Types是一个用来处理TypeMirror的工具
//`Elements`:  Elements是一个用来处理Element的工具
//`Filer`: 一般我们会用它配合JavaPoet来生成我们需要的.java文件
//`Messager`: Messager提供给注解处理器一个报告错误、警告以及提示信息的途径

        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement element : annotations) {
            //新建文件
            if (element.getQualifiedName().toString().equals(MyAnnotation.class.getCanonicalName())) {
                // 创建main方法
                MethodSpec main = MethodSpec.methodBuilder("main")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(String[].class, "args")
                        .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                        .build();
                // 创建HelloWorld类
                TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(main)
                        .build();

                try {
                    // 生成 com.example.HelloWorld.java
                    JavaFile javaFile = JavaFile.builder("com.example", helloWorld)
                            .addFileComment(" This codes are generated automatically. Do not modify!")
                            .build();
                    //　生成文件
                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //在Gradle console 打印日志
        for (Element element : roundEnv.getElementsAnnotatedWith(MyAnnotation.class)) {
            System.out.println("------------------------------");
            // 判断元素的类型为Class
            if (element.getKind() == ElementKind.CLASS) {
                // 显示转换元素类型
                TypeElement typeElement = (TypeElement) element;
                // 输出元素名称
                System.out.println(typeElement.getSimpleName());
                // 输出注解属性值
                System.out.println(typeElement.getAnnotation(MyAnnotation.class).value());
            }
            System.out.println("------------------------------");
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(MyAnnotation.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
