import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import './header.css';

const Header = () => {
  const [menuOpen, setMenuOpen] = useState(false); // 드롭다운 메뉴 열림 상태

  // 메뉴 열기
  const handleMenuEnter = () => setMenuOpen(true);

  // 메뉴 닫기
  const handleMenuLeave = () => setMenuOpen(false);

  return (
    <header className="header">
      {/* 로고와 햄버거 메뉴 */}
      <div className="logo-menu">
        <Link to="/" className="logo-text">MigrateMate</Link>
        <div
          className="hamburger-menu"
          onMouseEnter={handleMenuEnter} // 마우스 올릴 때 열림
          onMouseLeave={handleMenuLeave} // 마우스 벗어날 때 닫힘
        >
          <i className="fas fa-bars"></i>

          {/* 드롭다운 메뉴 */}
          {menuOpen && (
            <div className="dropdown-menu">
              <Link to="/about" className="menu-item">소개</Link>
              <div className="menu-item dropdown">
                DMS 작업 관리
                <div className="submenu">
                  <Link to="/create" className="submenu-item">DMS 작업 생성</Link>
                  <Link to="/schedule" className="submenu-item">주기 설정</Link>
                  <Link to="/rds" className="submenu-item">RDS 생성</Link>
                  <Link to="/list" className="submenu-item">작업 목록</Link>
                </div>
              </div>
              <Link to="/data-validation" className="menu-item">데이터 검증</Link>
              <Link to="/reports-logs" className="menu-item">리포트 및 로그</Link>
              <Link to="/retry-recovery" className="menu-item">재시도 및 복구</Link>
              <Link to="/template-management" className="menu-item">템플릿 관리</Link>
            </div>
          )}
        </div>
      </div>

      {/* 오른쪽 아이템 */}
      <div className="right-items">
        <a href="/login" className="login-link">Login</a>
        <i className="fas fa-cogs"></i>
        <select className="language-dropdown">
          <option value="Korean">한국어</option>
          <option value="English">English</option>
          <option value="Chinese">中文</option>
          <option value="Japanese">日本語</option>
          <option value="Spanish">Español</option>
        </select>
      </div>
    </header>
  );
};

export default Header;
