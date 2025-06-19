package com.barbershop.features.user.controller;

import com.barbershop.common.util.ApiConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.USER_API_BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones relacionadas de usuarios")
public class UserController {

    @GetMapping(ApiConstants.USER_ALL_URL)
    public String findAll(){
        return "Todos los usuarios";
    }

}
