import React from 'react';
import './introduction.css'; // 스타일링을 위한 별도 CSS 파일

const Introduction = () => {
  return (
    <div className="introduction-container">
      {/* 헤더 섹션 */}
      <header className="intro-header">
        <p className="info"> 로그인하지 않은 상태에서의 홈 화면(임시). 로그인 후에는 대시보드 or 소개 페이지로 리다이렉트(하면 어떨까 하는 생각) </p>
        <h1 className="intro-title">데이터 마이그레이션, 이제는 더 간편하게</h1>
        <p className="intro-subtitle">
          데이터베이스를 안전하게, 복잡한 수작업 없이 클라우드로 이전하세요.
        </p>
        <p className="intro-description">
          MigrateMate는 AWS DMS의 강력한 기능을 자동화하여 신속하고 효율적인 마이그레이션 경험을 제공합니다.
          안정적이고 간편한 데이터 이전 솔루션을 지금 만나보세요.
        </p>
      </header>

      {/* 주요 기능 섹션 */}
      <section className="features-section">
        <h2 className="features-title">주요 기능</h2>
        <div className="features-list">
          <div className="feature-item">
            <h3 className="feature-title">DMS 작업의 간편화</h3>
            <p className="feature-description">
              DMS 작업을 위한 정보를 입력하면 자동으로 작업이 시작됩니다.
            </p>
          </div>
          <div className="feature-item">
            <h3 className="feature-title">자동 반복 마이그레이션</h3>
            <p className="feature-description">
              주기적인 DMS 작업이 필요한 경우 자동 반복 주기를 설정할 수 있습니다.
            </p>
          </div>
          <div className="feature-item">
            <h3 className="feature-title">데이터 무결성 검증</h3>
            <p className="feature-description">
              원본과 대상 데이터베이스 간 데이터 일관성을 검증하여 안정적인 마이그레이션을 보장합니다.
            </p>
          </div>
          <div className="feature-item">
            <h3 className="feature-title">비용 관리 및 알림</h3>
            <p className="feature-description">
              과금 방지를 위한 리마인드 기능을 제공합니다.
            </p>
          </div>
          <div className="feature-item">
            <h3 className="feature-title">마이그레이션 보고서 자동 생성</h3>
            <p className="feature-description">
              DMS 작업 종료 후 요약 리포트 및 상세 로그를 제공합니다.
            </p>
          </div>
          <div className="feature-item">
            <h3 className="feature-title">마이그레이션 템플릿 저장</h3>
            <p className="feature-description">
              자주 사용하는 마이그레이션 설정을 템플릿으로 저장하여 재사용할 수 있습니다.
            </p>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Introduction;