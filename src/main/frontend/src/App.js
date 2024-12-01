import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import axios from 'axios';
import Dms from './components/dms';
import Header from './components/header';
import Introduction from './components/introduction';
import Tags from './components/tags';

function App() {
  const [hidata, setHello] = useState('');

  return (
    <Router>
      {/* 헤더를 Routes 외부에 배치하여 모든 페이지에서 표시되도록 */}
      <Header />

      <Routes>
        <Route path="/" element={<Introduction />} />
        <Route path="/create" element={<Dms />} />
        <Route path="/tags" element={<Tags />} />
      </Routes>
    </Router>
  );
}

export default App;