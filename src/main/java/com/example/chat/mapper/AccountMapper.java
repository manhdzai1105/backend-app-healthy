package com.example.chat.mapper;

import com.example.chat.dto.req.UpdateAccountRequest;
import com.example.chat.dto.req.UpdateDoctorRequest;
import com.example.chat.dto.res.AccountResponse;
import com.example.chat.dto.res.DoctorResponse;
import com.example.chat.entity.Account;
import com.example.chat.entity.DoctorDetail;
import com.example.chat.entity.UserDetail;
import org.mapstruct.*;

@org.mapstruct.Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateAccountFromDTO(UpdateAccountRequest accountDTO, @MappingTarget Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    void updateUserDetailFromDTO(UpdateAccountRequest userDetailDto, @MappingTarget UserDetail userDetail);

    @Mapping(source = "userDetail.phone", target = "phone")
    @Mapping(source = "userDetail.gender", target = "gender")
    @Mapping(source = "userDetail.date_of_birth", target = "date_of_birth")
    @Mapping(source = "userDetail.avatar_url", target = "avatar_url")
    AccountResponse toDto(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDoctorFromDTO(UpdateDoctorRequest accountDTO, @MappingTarget Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDoctorDetailFromDTO(UpdateDoctorRequest doctorDetailDto, @MappingTarget DoctorDetail doctorDetail);

    @Mapping(source = "doctorDetail.phone", target = "phone_number")
    @Mapping(source = "doctorDetail.gender", target = "gender")
    @Mapping(source = "doctorDetail.date_of_birth", target = "date_of_birth")
    @Mapping(source = "doctorDetail.avatar_url", target = "avatar_url")
    @Mapping(source = "doctorDetail.specialization", target = "specialization")
    @Mapping(source = "doctorDetail.experienceYears", target = "experience_years")
    @Mapping(source = "doctorDetail.bio", target = "bio")
    @Mapping(source = "doctorDetail.fee", target = "fee")
    DoctorResponse toDoctorDto(Account account);

}
