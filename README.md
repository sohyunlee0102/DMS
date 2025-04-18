# **Cloud-based Data Migration Project**

![image](https://github.com/user-attachments/assets/a436698e-732b-4339-a906-35976985cd7f)
![image](https://github.com/user-attachments/assets/4070ec65-143b-486b-a727-79a27d53353f)
![image](https://github.com/user-attachments/assets/8f0b4eae-582f-4a17-922c-86416fee5a34)


## 📄 **Overview**
🐰 In the Cloud-based Data Migration project, I took on the role of backend development (Migration, Spring Boot).

## 📝 **Introduction**

As cloud services grow in convenience, the demand for data migration services to the cloud has increased. Cloud offers the advantage of minimizing IT infrastructure management and efficiently managing information, which is even more critical due to the continuous growth of data and the expansion of online services. This has made AWS’s Data Migration Service (DMS) a crucial tool.

Through this major project, I directly experienced the data migration process. After building the database, I used AWS’s cloud features to migrate data to the cloud through DMS. Especially for heterogeneous database migrations, I created script files to perform the conversion and implemented a webpage to simplify the process.

Currently, I’m refactoring the project to improve the shortcomings of DMS and developing a web service called “MigrateMate” that offers additional data analysis features. This service is mainly targeted at students or small/new businesses who may not have access to professional solutions architects.

## Main Goals [Major Project]
- On-premise (Windows, Ubuntu) → RDS Homogeneous Migration
- EC2 → RDS Homogeneous Migration
- Heterogeneous Migration via export and import of database script files
- Development of a web page to facilitate migration using script files

## Main Features [MigrateMate]
- Simplifies DMS operations by inputting Source Endpoint, Target Endpoint, and Replication Instance information and using AccessKey and SecretKey (Completed)
- Migration validation and report generation
- Multi-cloud migration (On-premise/AWS → AWS/GCP/Azure)
- Notification system to prevent overcharging

## ⚙️ System Architecture   
![image](https://github.com/user-attachments/assets/5a5f9654-c3c6-4110-963e-a5f4dd6648dc)   

## 🖥️ **What I did**

- Created RDS MariaDB server
- Performed homogeneous migration (On-premise Ubuntu MySQL → AWS RDS MySQL) using DMS
- Used script files for heterogeneous migration (Ubuntu OracleDB → AWS RDS MariaDB)
- Set up MySQL and MariaDB virtual servers using Docker
- Developed DMS automation webpage & features [MigrateMate]
   
## 📕 **Learned**

1. Gained a deep understanding of cloud and containerization technologies such as RDS, DMS, and Docker.
2. Solved network issues, including opening firewalls, setting AWS security groups, and binding MySQL for all IPs, expanding my knowledge of computer architecture.
3. Realized the importance of port forwarding to access external endpoints (actual DB servers) for data migration, and set up port forwarding on a real router in a Wi-Fi environment.
4. Designed the execution process for [MigrateMate], linking Spring Boot → AWS CLI → Terraform to directly create and test DMS resources.
5. Implemented real-time log transmission to the frontend using Spring Boot’s SseEmitter, allowing users to track progress and error causes in real-time.
6. Improved my problem-solving ability by using official documentation to resolve issues when Google resources were scarce for [MigrateMate] regarding Terraform and AWS SDK.

## 🚀 **Technology Stack**

- **Used Stacks**: MySQL, MariaDB, Oracle, Spring Boot, React
- **Used Tools**: Git, IntelliJ, EC2, RDS, DMS, Docker, Terraform, Figma
- **Used Collaborations**: Notion, GitHub

---

# MigrateMate
MigrateMate : AWS DMS를 활용한 클라우드 기반 데이터 마이그레이션 자동화 서비스
  
## Push
1. push는 반드시 main이 아닌 새로운 브랜치에 파서 진행 (Ex: `sohyun/login`)
2. 이후 PR을 통해 main으로 merge
  
## 테스트 시 주의사항
회원가입/로그인 구현 전 테스트를 위해 `accessKey` 및 `secretKey`를 환경 변수로 설정해야 합니다.
    
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
