import React, { useState } from 'react';
import axios from 'axios';
import './dms2.css';

function Dms2() {
  // 소스 엔드포인트 관련 상태 변수
  const [sourceEndpointId, setSourceEndpointId] = useState('');
  const [sourceUsername, setSourceUsername] = useState('');
  const [sourcePassword, setSourcePassword] = useState('');
  const [sourceDatabaseName, setSourceDatabaseName] = useState('');
  const [sourceServerName, setSourceServerName] = useState('');
  const [sourcePort, setSourcePort] = useState('');
  const [sourceEngine, setSourceEngine] = useState('mysql');
  const [sourceTags, setSourceTags] = useState('');

  // 타겟 엔드포인트 관련 상태 변수
  const [targetEndpointId, setTargetEndpointId] = useState('');
  const [targetUsername, setTargetUsername] = useState('');
  const [targetPassword, setTargetPassword] = useState('');
  const [targetDatabaseName, setTargetDatabaseName] = useState('');
  const [targetServerName, setTargetServerName] = useState('');
  const [targetPort, setTargetPort] = useState('');
  const [targetEngine, setTargetEngine] = useState('mysql');
  const [targetTags, setTargetTags] = useState('');

  // 복제 인스턴스 관련 상태 변수
  const [replicationInstanceName, setReplicationInstanceName] = useState('');
  const [replicationInstanceClass, setReplicationInstanceClass] = useState('');
  const [replicationEngineVersion, setReplicationEngineVersion] = useState('');
  const [highAvailability, setHighAvailability] = useState(false);
  const [storage, setStorage] = useState('');
  const [vpc, setVpc] = useState('default');
  const [subnetGroup, setSubnetGroup] = useState('default');
  const [publicAccessible, setPublicAccessible] = useState(false);
  const [replicationInstanceTags, setReplicationInstanceTags] = useState('');

  // DMS 태스크 관련 상태 변수
  const [taskName, setTaskName] = useState('');
  const [targetTablePreparationMode, setTargetTablePreparationMode] = useState('DO_NOTHING');
  const [lobColumnSettings, setLobColumnSettings] = useState('');
  const [maxLobSize, setMaxLobSize] = useState('');
  const [dataValidation, setDataValidation] = useState(true);
  const [taskLogs, setTaskLogs] = useState(true);
  const [dmsTaskTags, setDmsTaskTags] = useState('');

  // 유효성 검사 함수
  const validateInputs = (inputs) => {
    for (const [key, value] of Object.entries(inputs)) {
      if (!value || value.trim() === '') {
        alert(`${key} is required`);
        return false;
      }
    }
    return true;
  };

  const handleCreateSourceEndpoint = () => {
    const sourceInputs = {
      'Source Endpoint ID': sourceEndpointId,
      'Source Username': sourceUsername,
      'Source Password': sourcePassword,
      'Source Server Name': sourceServerName,
      'Source Port': sourcePort,
    };

    if (!validateInputs(sourceInputs)) return;

    const tagsArray = sourceTags.split(',').map(tag => tag.trim()).filter(tag => tag !== '');

    axios.post('http://localhost:8080/dms/create-source-endpoint', {
      endpointId: sourceEndpointId,
      username: sourceUsername,
      password: sourcePassword,
      serverName: sourceServerName,
      port: sourcePort,
      engine: sourceEngine,
      tags: tagsArray
    })
    .then(response => {
      alert('Source Endpoint Created');
    })
    .catch(error => {
      console.error(error);
      alert('Error creating source endpoint');
    });
  };

  const handleCreateTargetEndpoint = () => {
    const targetInputs = {
      'Target Endpoint ID': targetEndpointId,
      'Target Username': targetUsername,
      'Target Password': targetPassword,
      'Target Server Name': targetServerName,
      'Target Port': targetPort,
    };

    if (!validateInputs(targetInputs)) return;

    const tagsArray = targetTags.split(',').map(tag => tag.trim()).filter(tag => tag !== '');

    axios.post('http://localhost:8080/dms/create-target-endpoint', {
      endpointId: targetEndpointId,
      username: targetUsername,
      password: targetPassword,
      serverName: targetServerName,
      port: targetPort,
      engine: targetEngine,
      tags: tagsArray
    })
    .then(response => {
      alert('Target Endpoint Created');
    })
    .catch(error => {
      console.error(error);
      alert('Error creating target endpoint');
    });
  };

  const handleCreateReplicationInstance = () => {
    const replicationInputs = {
      'Replication Instance Name': replicationInstanceName,
      'Replication Instance Class': replicationInstanceClass,
      'Engine Version': replicationEngineVersion,
      'Storage': storage,
    };

    if (!validateInputs(replicationInputs)) return;

    const tagsArray = replicationInstanceTags.split(',').map(tag => tag.trim()).filter(tag => tag !== '');

    axios.post('http://localhost:8080/dms/create-replication-instance', {
      instanceName: replicationInstanceName,
      instanceClass: replicationInstanceClass,
      engineVersion: replicationEngineVersion,
      highAvailability: highAvailability,
      storage: storage,
      vpc: vpc,
      subnetGroup: subnetGroup,
      publicAccessible: publicAccessible,
      tags: tagsArray
    })
    .then(response => {
      alert('Replication Instance Created');
    })
    .catch(error => {
      console.error(error);
      alert('Error creating replication instance');
    });
  };

  const handleCreateDmsTask = () => {
    const taskInputs = {
      'Task Name': taskName,
    };

    if (!validateInputs(taskInputs)) return;

    const tagsArray = dmsTaskTags.split(',').map(tag => tag.trim()).filter(tag => tag !== '');

    axios.post('http://localhost:8080/dms/create-dms-task', {
      taskName: taskName,
      targetTablePreparationMode: targetTablePreparationMode,
      lobColumnSettings: lobColumnSettings,
      maxLobSize: maxLobSize,
      dataValidation: dataValidation,
      taskLogs: taskLogs,
      tags: tagsArray
    })
    .then(response => {
      alert('DMS Task Created');
    })
    .catch(error => {
      console.error(error);
      alert('Error creating DMS task');
    });
  };

  return (
    <div>
      <h1>AWS DMS Task Automation</h1>

      {/* Source Endpoint Form */}
      <h2>Create Source Endpoint</h2>
      <form onSubmit={(e) => e.preventDefault()}>
        <input
          type="text"
          placeholder="Source Username"
          value={sourceUsername}
          onChange={(e) => setSourceUsername(e.target.value)}
        />
        <input
          type="password"
          placeholder="Source Password"
          value={sourcePassword}
          onChange={(e) => setSourcePassword(e.target.value)}
        />
        <input
          type="text"
          placeholder="Source Endpoint Id"
          value={sourceEndpointId}
          onChange={(e) => setSourceEndpointId(e.target.value)}
        />
        <input
          type="text"
          placeholder="Source Server Name"
          value={sourceServerName}
          onChange={(e) => setSourceServerName(e.target.value)}
        />
        <input
          type="number"
          placeholder="Source Port"
          value={sourcePort}
          onChange={(e) => setSourcePort(e.target.value)}
        />
        <select value={sourceEngine} onChange={(e) => setSourceEngine(e.target.value)}>
          <option value="mysql">MySQL</option>
          <option value="postgres">PostgreSQL</option>
          <option value="oracle">Oracle</option>
          <option value="sqlserver">SQL Server</option>
          <option value="mariadb">MariaDB</option>
        </select>
        <input
          type="text"
          placeholder="Source Tags"
          value={sourceTags}
          onChange={(e) => setSourceTags(e.target.value)}
        />
        <button type="button" onClick={handleCreateSourceEndpoint}>Create Source Endpoint</button>
      </form>

      {/* Target Endpoint Form */}
      <h2>Create Target Endpoint</h2>
      <form onSubmit={(e) => e.preventDefault()}>
        <input
          type="text"
          placeholder="Target Username"
          value={targetUsername}
          onChange={(e) => setTargetUsername(e.target.value)}
        />
        <input
          type="password"
          placeholder="Target Password"
          value={targetPassword}
          onChange={(e) => setTargetPassword(e.target.value)}
        />
        <input
          type="text"
          placeholder="Target Endpoint Id"
          value={targetEndpointId}
          onChange={(e) => setTargetEndpointId(e.target.value)}
        />
        <input
          type="text"
          placeholder="Target Server Name"
          value={targetServerName}
          onChange={(e) => setTargetServerName(e.target.value)}
        />
        <input
          type="number"
          placeholder="Target Port"
          value={targetPort}
          onChange={(e) => setTargetPort(e.target.value)}
        />
        <select value={targetEngine} onChange={(e) => setTargetEngine(e.target.value)}>
          <option value="mysql">MySQL</option>
          <option value="postgres">PostgreSQL</option>
          <option value="oracle">Oracle</option>
          <option value="sqlserver">SQL Server</option>
          <option value="mariadb">MariaDB</option>
        </select>
        <input
          type="text"
          placeholder="Target Tags"
          value={targetTags}
          onChange={(e) => setTargetTags(e.target.value)}
        />
        <button type="button" onClick={handleCreateTargetEndpoint}>Create Target Endpoint</button>
      </form>

      {/* Replication Instance Form */}
      <h2>Create Replication Instance</h2>
      <form onSubmit={(e) => e.preventDefault()}>
        <input
          type="text"
          placeholder="Replication Instance Name"
          value={replicationInstanceName}
          onChange={(e) => setReplicationInstanceName(e.target.value)}
        />
        <input
          type="text"
          placeholder="Replication Instance Class"
          value={replicationInstanceClass}
          onChange={(e) => setReplicationInstanceClass(e.target.value)}
        />
        <input
          type="text"
          placeholder="Engine Version"
          value={replicationEngineVersion}
          onChange={(e) => setReplicationEngineVersion(e.target.value)}
        />
        <label>
          High Availability
          <input
            type="checkbox"
            checked={highAvailability}
            onChange={(e) => setHighAvailability(e.target.checked)}
          />
        </label>
        <input
          type="number"
          placeholder="Storage (GB)"
          value={storage}
          onChange={(e) => setStorage(e.target.value)}
        />
        <select value={vpc} onChange={(e) => setVpc(e.target.value)}>
          <option value="default">Default VPC</option>
        </select>
        <select value={subnetGroup} onChange={(e) => setSubnetGroup(e.target.value)}>
          <option value="default">Default Subnet Group</option>
        </select>
        <label>
          Publicly Accessible
          <input
            type="checkbox"
            checked={publicAccessible}
            onChange={(e) => setPublicAccessible(e.target.checked)}
          />
        </label>
        <input
          type="text"
          placeholder="Replication Instance Tags"
          value={replicationInstanceTags}
          onChange={(e) => setReplicationInstanceTags(e.target.value)}
        />
        <button type="button" onClick={handleCreateReplicationInstance}>Create Replication Instance</button>
      </form>

      {/* DMS Task Form */}
      <h2>Create DMS Task</h2>
      <form onSubmit={(e) => e.preventDefault()}>
        <input
          type="text"
          placeholder="Task Name"
          value={taskName}
          onChange={(e) => setTaskName(e.target.value)}
        />
        <select value={targetTablePreparationMode} onChange={(e) => setTargetTablePreparationMode(e.target.value)}>
          <option value="DO_NOTHING">Do Nothing</option>
          <option value="DROP_AND_CREATE">Drop and Create</option>
          <option value="TRUNCATE">Truncate</option>
        </select>
        <input
          type="text"
          placeholder="LOB Column Settings"
          value={lobColumnSettings}
          onChange={(e) => setLobColumnSettings(e.target.value)}
        />
        <input
          type="number"
          placeholder="Max LOB Size"
          value={maxLobSize}
          onChange={(e) => setMaxLobSize(e.target.value)}
        />
        <label>
          Data Validation
          <input
            type="checkbox"
            checked={dataValidation}
            onChange={(e) => setDataValidation(e.target.checked)}
          />
        </label>
        <label>
          Task Logs
          <input
            type="checkbox"
            checked={taskLogs}
            onChange={(e) => setTaskLogs(e.target.checked)}
          />
        </label>
        <input
          type="text"
          placeholder="DMS Task Tags"
          value={dmsTaskTags}
          onChange={(e) => setDmsTaskTags(e.target.value)}
        />
        <button type="button" onClick={handleCreateDmsTask}>Create DMS Task</button>
      </form>
    </div>
  );
}

export default Dms2;
