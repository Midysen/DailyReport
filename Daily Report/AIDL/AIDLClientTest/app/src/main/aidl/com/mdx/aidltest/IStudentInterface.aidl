// IStudentInterface.aidl
package com.mdx.aidltest;

// Declare any non-default types here with import statements
import com.mdx.aidltest.Student;
import com.mdx.aidltest.ITaskCallback;

interface IStudentInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

     void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

     void register(ITaskCallback callback);
     void unregister(ITaskCallback callback);
     List<Student> getStudentList();
     void addStudent(in Student stu);
}
