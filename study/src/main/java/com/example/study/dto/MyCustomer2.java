package com.example.study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyCustomer2 {

    @NotNull(message = "First name is required")
    @Pattern(regexp = "[a-zA-Z]+", message = "First name is must be alphabetical")
    private String firstName;

    @Size(min = 1, max = 1)
    @Pattern(regexp = "[a-zA-Z]+", message = "Middle Initial is must be alphabetical")
    private String middleInitial;

    @NotNull(message = "Last name is required")
    @Pattern(regexp = "[a-zA-Z]+", message = "Last Name is must be alphabetical")
    private String lastName;

    @NotNull(message = "address is required")
    @Pattern(regexp = "[0-9a-zA-Z\\. ]+")
    private String address;

    @NotNull(message = "city is required")
    @Pattern(regexp = "[a-zA-Z\\. ]+")
    private String city;

    @NotNull(message = "state is required")
    @Size(min=2, max=2)
    @Pattern(regexp = "[A-Z]{2}")
    private String state;

    @NotNull(message = "zipCode is required")
    @Size(min=5,max=5)
    @Pattern(regexp = "\\d{5}")
    private String zipCode;
}
