package com.svj.proj;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

public class FunctionClass {
//    SELECT COUNT(*) as total FROM empTable emp GROUP BY empTable.Country
static class FreqCount {
        String country;
        int cout;

    public FreqCount(String country, int cout) {
        this.country= country;
        this.cout= cout;
    }
}
    @Data
    static
    class Employee{
        String country;
        String name;
        public Employee(String country, String name){
            this.country= country;
            this.name= name;
        }
    }

    public static List<FreqCount> groupEmpByCountry(List<Employee> employees){
        Map<String, List<Employee>> countryMap = employees.stream()
                .collect(groupingBy(Employee::getCountry));
        List<FreqCount> list= new LinkedList<>();
        for(String country: countryMap.keySet()){
            list.add(new FreqCount(country, countryMap.get(country).size()));
        }
        return list;
    }
    public boolean m1(String s){
        // len= odd madam, l= 5
        // maddam, l=6
        int mid= s.length()/2;
//        for(int i=0; i<mid; i++){
//            if(s.charAt(mid-i)!= s.charAt((mid+i))){
//                return false;
//            }
//        }
//        return true;
        int l= s.length()-1;
        for(int i=0; i<mid; i++){
            if(s.charAt(i)!= s.charAt(l-i)){
                return false;
            }
        }
        return true;
    }



}
