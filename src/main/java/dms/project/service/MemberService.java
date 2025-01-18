package dms.project.service;

import dms.project.domain.Member;
import dms.project.dto.MemberRequestDto;
import dms.project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    @Autowired
    private final MemberRepository memberRepository;

    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;

    public Member createMember(MemberRequestDto.JoinDto request) {
        String encryptedPassword = passwordEncoder.encode(request.getPassword()); // 비밀번호 암호화
        Member member = Member.builder()
                .name(request.getName()) // 이름
                .email(request.getEmail()) // 이메일
                .password(encryptedPassword) // 암호화된 비밀번호
                .role(request.getRole()) // 역할
                .build();

        return memberRepository.save(member); // DB에 저장
    }
}
