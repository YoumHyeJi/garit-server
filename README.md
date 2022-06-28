운영 서버 만들기 예제

# 1차 자동 배포 (2022-06-29)
- deploy.yml 파일 생성
- appspec.yml 파일 생성
- deploy.sh 파일 생성

# 자동 배포 에러 해결
- EC2에 CodeDeploy 관련 IAM Role이 부여되기 전에 CodeDeploy Agent가 실행되면서 IAM Role을 못 가져간 에러 발생
- sudo service codedeploy-agent restart 명령어로 해결

# 자동 배포 에러 해결
- deploy.sh 파일을 다음과 같이 수정 
- activce profile : dev

