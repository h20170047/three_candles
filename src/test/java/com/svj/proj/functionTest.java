package com.svj.proj;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class functionTest {
    @Test
    public void tesOddString(){
        FunctionClass obj= new FunctionClass();
        assertThat(obj.m1("madam")).isTrue();
        assertThat(obj.m1("maddam")).isTrue();
    }

    @Test
    public void countryCount(){
        FunctionClass testClass= new FunctionClass();
        List<FunctionClass.Employee> employees= Arrays.asList(
                new FunctionClass.Employee("India", "Ram"),
                new FunctionClass.Employee("India", "Kumar"),
                new FunctionClass.Employee("India", "Ali")
        );
        List<FunctionClass.FreqCount> freqCounts = FunctionClass.groupEmpByCountry(employees);
        assertThat(freqCounts.get(0).country).isEqualTo("India");
        assertThat(freqCounts.get(0).cout).isEqualTo(3);
    }

}