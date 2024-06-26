## 프로젝트 소개
이 프로젝트는 가상화폐 거래소 프로젝트입니다. 본 거래소는 마진 거래(선물 거래) 기능을 중심으로 구현되어 있으며, 
사용자는 다양한 가상화폐에 대해 레버리지를 적용한 마진거래를 실시할 수 있습니다. 거래소의 코인 가격은 내부 거래소의 거래 외에도
외부 실시간 데이터에 반응하여 업데이트되므로, 사용자는 시장의 변화에 빠르게 대응하면서 트레이딩 전략을 시험해 볼 수 있습니다.

## 기능
- 사용자 등록 및 로그인 : 사용자는 계정을 생성하고 로그인 할 수 있습니다.
- 포트폴리오 관리: 사용자는 자신의 계좌를 통해 현재 자신의 거래 포지션과 지갑 상태등을 확인할 수 있습니다.
- 레버리지 거래 지원: 사용자는 선택한 코인에 대해 레버리지를 설정하여 매수, 매도 거래를 할 수 있습니다. 시장 가격에 따라 포지션이 청산당할 수 있습니다.
- 다양한 거래 옵션 지원: 사용자는 시장가 또는 지정가로 거래할 수 있으며, 금액이나 수량을 기준으로 거래를 설정할 수 있습니다.
- 실시간 가격 확인: 사용자는 거래소 내의 거래 외에도 외부 거래소의 실시간 가격 데이터를 연동하여 실시간으로 변동하는 코인 가격을 확인할 수 있습니다.

## 기술 스택

### 백엔드
- **Spring Boot**
- **Spring Data JPA**
- **Spring WebSocket**

### 프론트엔드
- **React**:

### 데이터베이스
- **H2 Database**: 추후 mysql로 옮길 예정

### 개발 도구
- **Gradle**
- **JUnit**

## 설치 방법
### 필요 조건
- Java 17 이상

### 설치 과정
1. 소스 코드 클론:
   ```bash
   git clone https://github.com/csh7733/virtualCurrency.git

## 스크린샷
<img src="https://github.com/csh7733/virtualCurrency/assets/149491102/cf03f89c-d6d5-4032-9b37-65f3448c66bc" width="500"/>
<img src="https://github.com/csh7733/virtualCurrency/assets/149491102/8c6857b5-e569-470c-bb05-807e8160aff8" width="500"/>
<img src="https://github.com/csh7733/virtualCurrency/assets/149491102/b83a1c19-6f99-4c35-803b-83ff370be2e2" width="500"/>
<img src="https://github.com/csh7733/virtualCurrency/assets/149491102/08ada0be-7859-4382-952e-bed076771404" width="500"/>
