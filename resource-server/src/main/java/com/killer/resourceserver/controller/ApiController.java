package com.killer.resourceserver.controller;

import com.killer.resourceserver.common.R;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author killer
 * @date 2019/07/25 - 21:34
 */
@RestController
@RequestMapping("api")
public class ApiController  {

    @GetMapping("test")
    public Map<String, Object> say(HttpServletRequest request, HttpServletResponse response) {
        return R.ok("Say OAuth2!");
    }

    @GetMapping("hello")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> hello() {
        return R.ok("Hello OAuth2");
    }

}
