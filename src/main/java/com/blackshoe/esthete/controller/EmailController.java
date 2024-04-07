package com.blackshoe.esthete.controller;

import com.blackshoe.esthete.dto.EmailDto;
import com.blackshoe.esthete.service.EmailSendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/signup/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailSendService mailService;

    @PostMapping("/validation")
    public String mailSend(@RequestBody @Valid EmailDto.EmailRequestDto emailDto) {
        System.out.println("이메일 인증 이메일 :" + emailDto.getEmail());
        return mailService.joinEmail(emailDto.getEmail());
    }

    @PostMapping("/verification")
    public String AuthCheck(@RequestBody @Valid EmailDto.EmailCheckDto emailCheckDto){
        Boolean Checked=mailService.CheckAuthNum(emailCheckDto.getEmail(),emailCheckDto.getAuthNum());
        if(Checked){
            return "인증 성공";
        }
        else{
            throw new NullPointerException("유효하지 않은 인증번호입니다.");
        }
    }
}

