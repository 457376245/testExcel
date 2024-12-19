package com.example.testexcel.test;

import com.example.testexcel.test.service.WebToExcel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;

import static com.example.testexcel.test.service.WebToExcel.ids;
import static com.example.testexcel.test.service.WebToExcel.tableDataList;

/**
 * @Program: testExcel
 * @ClassName: testController
 * @Author: JH
 * @Date: 2024-07-12 22:21
 * @Description:
 */
@Controller
public class testController {
    @Autowired
    WebToExcel webToExcel;

    @GetMapping("/test")
    public String test() throws Exception {
        return webToExcel.test();
    }

    @GetMapping("/login/{username}/{password}")
    public void login(@PathVariable String username, @PathVariable String password) throws Exception {
        webToExcel.getToken(username, password);
    }

    @GetMapping("/empty")
    public void empty() {
        webToExcel.empty();
    }

    @GetMapping("/data")
    public Map getData() {
        Map<String, Object> map = new HashMap<>();
        map.put("ids",ids);
        map.put("tableData",tableDataList);
        return map;
    }
}
