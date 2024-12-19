# MigrateMate
MigrateMate : AWS DMS를 활용한 클라우드 기반 데이터 마이그레이션 자동화 서비스
  
## Push
1. push는 반드시 main이 아닌 새로운 브랜치에 파서 진행 (Ex: `sohyun/login`)
2. 이후 PR을 통해 main으로 merge
  
## 테스트 시 주의사항
- 회원가입/로그인 구현 전 테스트를 위해 `accessKey` 및 `secretKey`를 환경 변수로 설정해야 합니다.
    
### Windows  
```bash
setx AWS_ACCESS_KEY_ID "your-access-key"
setx AWS_SECRET_ACCESS_KEY "your-secret-access-key"
```

### macOS/Linux
```bash
export AWS_ACCESS_KEY_ID="your-access-key"
export AWS_SECRET_ACCESS_KEY="your-secret-access-key"
```

### Docker
```bash
docker run -e AWS_ACCESS_KEY_ID=your-access-key -e AWS_SECRET_ACCESS_KEY=your-secret-access-key my-docker-image
```
