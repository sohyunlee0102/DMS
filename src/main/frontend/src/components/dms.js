import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './dms.css';
import Tags from './tags';

function Dms() {
  // 소스 엔드포인트 관련 상태 변수
  const [sourceEndpointId, setSourceEndpointId] = useState('');
  const [sourceUsername, setSourceUsername] = useState('');
  const [sourcePassword, setSourcePassword] = useState('');
  const [sourceServerName, setSourceServerName] = useState('');
  const [sourcePort, setSourcePort] = useState('');
  const [sourceEngine, setSourceEngine] = useState('mysql');
  const [tags, setTags] = useState([]);
  const [existingEndpoints, setExistingEndpoints] = useState([]);
  const [formType, setFormType] = useState('new'); // 'new' 또는 'existing'
  const [selectedEndpointId, setSelectedEndpointId] = useState('');
  const [selectedEndpointArn, setSelectedEndpointArn] = useState('');
  const [loading, setLoading] = useState(false);
  const [selectedEndpoint, setSelectedEndpoint] = useState('');
  const [selectedRegion, setSelectedRegion] = useState('ap-northeast-2'); // 기본 리전 설정

  const [targetEndpointId, setTargetEndpointId] = useState('');
  const [targetUsername, setTargetUsername] = useState('');
  const [targetPassword, setTargetPassword] = useState('');
  const [targetServerName, setTargetServerName] = useState('');
  const [targetPort, setTargetPort] = useState('');
  const [targetEngine, setTargetEngine] = useState('mysql');
  const [targetTags, setTargetTags] = useState([]);
  const [existingTargetEndpoints, setExistingTargetEndpoints] = useState([]);
  const [targetFormType, setTargetFormType] = useState('newTarget'); // 'new' 또는 'existing'
  const [targetLoading, setTargetLoading] = useState(false);
  const [selectedTargetEndpointId, setSelectedTargetEndpointId] = useState('');
  const [selectedTargetEndpointArn, setSelectedTargetEndpointArn] = useState('');
  const [selectedTargetEndpoint, setSelectedTargetEndpoint] = useState('');
  const [selectedTargetRegion, setSelectedTargetRegion] = useState('ap-northeast-2'); // 기본 리전 설정

  const [replicationInstanceId, setReplicationInstanceId] = useState('');
  const [replicationInstanceClass, setReplicationInstanceClass] = useState('');
  const [replicationEngineVersion, setReplicationEngineVersion] = useState('');
  const [highAvailability, setHighAvailability] = useState(false);
  const [storage, setStorage] = useState('');
  const [publicAccessible, setPublicAccessible] = useState(false);
  const [RITags, setRITags] = useState([]);
  const [RIFormType, setRIFormType] = useState('newRI'); // 'new' 또는 'existing'
  const [RILoading, setRILoading] = useState(false);
  const [selectedRIId, setSelectedRIId] = useState('');
  const [selectedRIArn, setSelectedRIArn] = useState('');
  const [selectedRI, setSelectedRI] = useState('');
  const [selectedRIRegion, setSelectedRIRegion] = useState('ap-northeast-2');
  const [existingRI, setExistingRI] = useState([]);
  const [riDescription, setRiDescription] = useState([]);
  const [vpcs, setVpcs] = useState([]);
  const [selectedVpc, setSelectedVpc] = useState("");
  const [isFetched, setIsFetched] = useState(false);
  const [vpcLoading, setVpcLoading] = useState(false);
  const [subnetGroups, setSubnetGroups] = useState([]);
  const [selectedSubnetGroup, setSelectedSubnetGroup] = useState("");
  const [isSubnetFetched, setIsSubnetFetched] = useState(false);
  const [securityGroups, setSecurityGroups] = useState(false);
  const [vpcSecurityGroups, setVpcSecurityGroups] = useState({});
  const [selectedSecurityGroup, setSelectedSecurityGroup] = useState("");
  const [RIs, setRIs] = useState("");

  const [taskName, setTaskName] = useState('');
  const [targetTablePreparationMode, setTargetTablePreparationMode] = useState('DO_NOTHING');
  const [lobColumnSettings, setLobColumnSettings] = useState('INLINE_LOB_MODE');
  const [maxLobSize, setMaxLobSize] = useState('');
  const [dataValidation, setDataValidation] = useState('OFF');
  const [taskLogs, setTaskLogs] = useState(false);
  const [dmsTaskTags, setDmsTaskTags] = useState([]);
  const [taskLoading, setTaskLoading] = useState(false);
  const [taskFormType, setTaskFormType] = useState('newTask');
  const [selectedTask, setSelectedTask] = useState('');
  const [existingTasks, setExistingTasks] = useState([]);
  const [migrationType, setMigrationType] = useState('full-load'); // 기본값
  const [startTaskOnCreation, setStartTaskOnCreation] = useState('true'); // 기본값
  const [tableMappings, setTableMappings] = useState([]);
  const [sourceSchema, setSourceSchema] = useState('');
  const [sourceTableName, setSourceTableName] = useState('');
  const [targetSchema, setTargetSchema] = useState('');
  const [targetTableName, setTargetTableName] = useState('');
  const [transformRules, setTransformRules] = useState([]);
  const [selectionRules, setSelectionRules] = useState([]);
  const [isTransformRuleFormVisible, setTransformRuleFormVisible] = useState(false);
  const [newTransformRule, setNewTransformRule] = useState('');
  const [selectedTaskId, setSelectedTaskId] = useState('');
  const [tasks, setTasks] = useState([]);
  const [arns, setArns] = useState([]);
  const [selectedTaskArn, setSelectedTaskArn] = useState([]);  // arn 배열

  const [createLoading, setCreateLoading] = useState(false);

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

 const handleCreateSourceEndpoint = async () => {
/*
   const sourceInputs = {
     'Source Endpoint ID': sourceEndpointId,
     'Source Username': sourceUsername,
     'Source Password': sourcePassword,
     'Source Server Name': sourceServerName,
     'Source Port': sourcePort,
     'Replication Instance': selectedRIArn,
   };

    const targetInputs = {
      'Source Endpoint ID': targetEndpointId,
      'Source Username': targetUsername,
      'Source Password': targetPassword,
      'Source Server Name': targetServerName,
      'Source Port': targetPort,
    };


   for (const [key, value] of Object.entries(sourceInputs)) {
     if (!value || value.trim() === '') {
       alert(`${key} is required`);
       return;
     }
   }

    for (const [key, value] of Object.entries(targetInputs)) {
      if (!value || value.trim() === '') {
        alert(`${key} is required`);
        return;
      }
    }


   // 입력값 유효성 검사 함수 (validateInputs)
   if (!validateInputs(sourceInputs)) {
     return;
   }

   if (!validateInputs(targetInputs)) {
     return;
   }


   // tags에 type을 'source'로 추가
   const tagsArray = tags.map(tag => ({
     key: tag.key,
     value: tag.value,
     type: 'source',  // type을 'source'로 설정
   }));

   const targetTagsArray = targetTags.map(targetTag => ({
     key: targetTag.key,
     value: targetTag.value,
     type: 'target',  // type을 'source'로 설정
   }));

   setCreateLoading(true);
   console.log(createLoading);

   try {
       console.log(selectedRIArn);
     const response = await axios.post('http://localhost:8080/dms/create-source-endpoint', {
       endpointId: sourceEndpointId,
       username: sourceUsername,
       password: sourcePassword,
       serverName: sourceServerName,
       port: sourcePort,
       engine: sourceEngine,
       tags: tagsArray,
       ReplicationInstance: selectedRIArn,
     });

     // 응답이 성공적이면
     if (response.status === 200) {
       alert('Source Endpoint Created');
     } else {
       alert('Error creating source endpoint');
     }
   } catch (error) {
     // 네트워크 오류나 예외 처리
     console.error(error);
     alert('Error creating source endpoint');
   } finally {
     setCreateLoading(false);
   }


    try {
        const response = await axios.post('http://localhost:8080/dms/create-target-endpoint', {
          endpointId: targetEndpointId,
          username: targetUsername,
          password: targetPassword,
          serverName: targetServerName,
          port: targetPort,
          engine: targetEngine,
          tags: targetTagsArray,
          ReplicationInstance: selectedRIArn,
        });

        // 응답이 성공적이면
        if (response.status === 200) {
          alert('Target Endpoint Created');
        } else {
          alert('Error creating source endpoint');
        }
      } catch (error) {
        // 네트워크 오류나 예외 처리
        console.error(error);
        alert('Error creating source endpoint');
      } finally {
        setCreateLoading(false);
      }

      try {

        setCreateLoading(true);

        const replicationInstanceData = {
          instanceName: replicationInstanceId,
          instanceClass: replicationInstanceClass,  // 리플리케이션 인스턴스 클래스
          storage: storage,  // 스토리지 용량
          engineVersion: replicationEngineVersion,  // 엔진 버전
          vpc: selectedSecurityGroup,  // 선택된 VPC ID
          tags: RITags,  // 리플리케이션 인스턴스 태그
          highAvailability: highAvailability,
          subnetGroup: selectedSubnetGroup,
          publicAccessible: publicAccessible,
          description: riDescription
        };

        console.log("selected : " + selectedSecurityGroup);

        const riResponse = await axios.post('http://localhost:8080/dms/create-replication-instance', replicationInstanceData);

        if (riResponse.status === 200) {
          alert('Replication Instance Created');
        } else {
          alert('Error creating replication instance');
        }
      } catch (error) {
        console.error('Error creating replication instance:', error);
        alert('Error creating replication instance');
      } finally {
        setCreateLoading(false);
      }
*/
     setCreateLoading(true);

     axios.post('http://localhost:8080/dms/start-task', {
         arn: selectedTaskArn,
         replicationInstanceArn: selectedRIArn,
         sourceEndpointArn: selectedEndpointArn,
         targetEndpointArn: selectedTargetEndpointArn
     })
         .then(response => {
             console.log('태스크가 성공적으로 시작되었습니다:', response.data);
             alert('태스크가 성공적으로 시작되었습니다.');
         })
         .catch(error => {
             console.error('태스크 시작 중 오류 발생:', error);
             alert('태스크 시작 중 오류가 발생했습니다.');
         })
         .finally(() => {
             setCreateLoading(false); // 로딩 종료
         });

 };


  const handleFetchEndpoints = () => {
    if (formType === 'existing' && selectedRegion) {
      setLoading(true); // 로딩 시작

      axios.get(`http://localhost:8080/dms/sourceEndpoints?region=${selectedRegion}`)
        .then(response => {
          const data = response.data;

          // 빈 배열일 경우: 해당 지역에 엔드포인트가 없음
          if (Array.isArray(data) && data.length === 0) {
            alert('해당 지역에 엔드포인트가 없습니다.');
            setExistingEndpoints([]); // 기존 엔드포인트 상태를 빈 배열로 초기화
          } else if (Array.isArray(data)) {
            setExistingEndpoints(data); // 정상 값이 반환되면 엔드포인트 목록 업데이트
          } else {
            // 정상 값이 아니면 오류
            alert('엔드포인트 데이터를 불러오는 데 오류가 발생했습니다.');
          }
        })
        .catch(error => {
          // 서버 요청이 실패하면 오류 메시지 표시
          console.error(error);
          alert('엔드포인트 데이터를 불러오는 데 오류가 발생했습니다.');
        })
        .finally(() => {
          setLoading(false); // 로딩 종료
        });
    }
  };

    const handleFetchRI = () => {
        if (selectedRegion) {  // Region이 선택되었는지 확인
            setRILoading(true); // 로딩 시작

            axios.get(`http://localhost:8080/dms/RIs?region=${selectedRegion}`)
                .then(response => {
                    const data = response.data;
                    console.log(data);

                    // 빈 배열일 경우: 해당 지역에 리플리케이션 인스턴스가 없음
                    if (Array.isArray(data) && data.length === 0) {
                        alert('해당 지역에 복제 인스턴스가 없습니다.');
                        setRIs([]); // 상태를 빈 배열로 초기화
                    } else if (Array.isArray(data)) {
                        setRIs(data); // 정상 값이 반환되면 목록 업데이트
                    } else {
                        // 정상 값이 아니면 오류
                        alert('복제 인스턴스 데이터를 불러오는 데 오류가 발생했습니다.');
                    }
                })
                .catch(error => {
                    // 서버 요청이 실패하면 오류 메시지 표시
                    console.error(error);
                    alert('복제 인스턴스 데이터를 불러오는 데 오류가 발생했습니다.');
                })
                .finally(() => {
                    setRILoading(false); // 로딩 종료
                });
        } else {
            alert('지역을 먼저 선택해주세요.'); // Region이 선택되지 않았을 경우
        }
    };

  const handleFetchTargetEndpoints = () => {
  console.log(formType + " " + selectedTargetRegion)

    if (targetFormType === 'existingTarget' && selectedTargetRegion) {
      setTargetLoading(true); // 로딩 시작
      console.log("HERE");

      axios.get(`http://localhost:8080/dms/targetEndpoints?region=${selectedTargetRegion}`)
        .then(response => {
          const data = response.data;

          // 빈 배열일 경우: 해당 지역에 엔드포인트가 없음
          if (Array.isArray(data) && data.length === 0) {
            alert('해당 지역에 엔드포인트가 없습니다.');
            setExistingTargetEndpoints([]); // 기존 엔드포인트 상태를 빈 배열로 초기화
          } else if (Array.isArray(data)) {
            setExistingTargetEndpoints(data); // 정상 값이 반환되면 엔드포인트 목록 업데이트
          } else {
            // 정상 값이 아니면 오류
            alert('엔드포인트 데이터를 불러오는 데 오류가 발생했습니다.');
          }
        })
        .catch(error => {
          // 서버 요청이 실패하면 오류 메시지 표시
          console.error(error);
          alert('엔드포인트 데이터를 불러오는 데 오류가 발생했습니다.');
        })
        .finally(() => {
          setTargetLoading(false); // 로딩 종료
        });
    }
  };

  const handleEndpointChange = (e, type) => {
    if (type === 'source') {
        const selectedId = e.target.value;
        const selected = existingEndpoints.find(endpoint => endpoint.id === selectedId);

        if (selected) {
          setSelectedEndpointId(selectedId);
          setSelectedEndpointArn(selected.arn);
        }
    } else if (type === 'target') {
        const selectedId = e.target.value;
        const selected = existingTargetEndpoints.find(endpoint => endpoint.id === selectedId);

        if (selected) {
          setSelectedTargetEndpointId(selectedId);
          setSelectedTargetEndpointArn(selected.arn);
        }
    } else if (type === 'RI') {
        const selectedId = e.target.value;
        const selected = RIs.find(RI => RI.name === selectedId);

        if (selected) {
            setSelectedRIId(selectedId);
            setSelectedRIArn(selected.arn);
        }
    } else if (type === 'task') {
        const selectedTaskId = e.target.value; // 선택된 taskIdentifier

        const index = existingTasks.indexOf(selectedTaskId);

        if (index !== -1) { // 유효한 taskIdentifier인 경우
            setSelectedTaskId(selectedTaskId); // taskIdentifier 업데이트
            setSelectedTaskArn(arns[index]); // 대응되는 arn 업데이트
        } else {
            console.log("Invalid selection");
        }
    }

  };

  const handleTagChange = (index, field, value, type) => {
    if (type === "source") {
      const newTags = [...tags];
      newTags[index][field] = value;
      setTags(newTags);
    } else if (type === "target") {
      const newTargetTags = [...targetTags];
      newTargetTags[index][field] = value;
      setTargetTags(newTargetTags);
    } else if (type === "RI") {
      const newRITags = [...RITags];
      newRITags[index][field] = value;
      setRITags(newRITags);
    } else if (type === "task") {
      const newTaskTags = [...dmsTaskTags];
      newTaskTags[index][field] = value;
      setDmsTaskTags(newTaskTags);
    }
  };

  const handleAddTag = (type) => {
    if (type === "source") {
      setTags([...tags, { key: "", value: "", type }]);
    } else if (type === "target") {
      setTargetTags([...targetTags, { key: "", value: "", type }]);
    } else if (type === "RI") {
      setRITags([...RITags, { key: "", value: "", type }]);
    } else if (type === "task") {
      setDmsTaskTags([...dmsTaskTags, { key: "", value: "", type }]);
    }
  };

  const handleRemoveTag = (index, type) => {
    if (type === "source") {
      setTags(tags.filter((_, i) => i !== index));
    } else if (type === "target") {
      setTargetTags(targetTags.filter((_, i) => i !== index));
    } else if (type === "RI") {
      setRITags(RITags.filter((_, i) => i !== index));
    } else if (type === "task") {
      setDmsTaskTags(dmsTaskTags.filter((_, i) => i !== index));
    }
  };

  useEffect(() => {
    fetchVpcs();
  }, []);  // 빈 배열로 설정하여 컴포넌트가 처음 렌더링될 때만 호출됨

  // 보안 그룹 선택이 변경될 때마다 출력
  useEffect(() => {
    if (selectedSecurityGroup) {
      console.log("Selected Security Group:", selectedSecurityGroup);
    }
  }, [selectedSecurityGroup]);

    const fetchVpcs = () => {
      axios.get('http://localhost:8080/dms/vpc')
        .then((response) => {
          const data = response.data;
          console.log('Fetched data:', data);

          if (Array.isArray(data)) {
            setVpcs(data);  // VPC 목록 저장
            console.log('VPCs:', data);

            // 만약 보안 그룹을 따로 설정해야 하는 경우
            data.forEach((vpc) => {
              console.log(`VPC: ${vpc.vpcId}, Security Group: ${vpc.securityGroup}`);
            });
          } else {
            console.error('Fetched data is not an array:', data);
            setVpcs([]);  // 데이터가 배열이 아닌 경우 빈 배열 설정
          }
        })
        .catch((error) => {
          console.error("Error fetching VPCs:", error);
        });
    };

  const fetchSubnetGroups = () => {
    if (!isSubnetFetched) {
      axios
        .get("http://localhost:8080/dms/subnet") // 서브넷 그룹 API 요청
        .then((response) => {
          const data = response.data;
          setSubnetGroups(data); // 서브넷 그룹 목록 업데이트
          setIsSubnetFetched(true); // 데이터 로드 완료 표시
        })
        .catch((error) => {
          console.error("Error fetching subnet groups:", error);
        });
    }
  };

    const handleVpcChange = (e) => {
      const vpcId = e.target.value;
      console.log("Selected VPC ID:", vpcId);
      setSelectedVpc(vpcId);  // 선택된 VPC ID 설정

      // 선택된 VPC에 해당하는 보안 그룹을 가져옴
      const selectedVpcData = vpcs.find(vpc => vpc.vpcId === vpcId);
      console.log(selectedVpcData);
      if (selectedVpcData) {
        // 해당 VPC의 보안 그룹을 설정 (여기서는 첫 번째 보안 그룹만 사용)
        const securityGroupId = selectedVpcData.securityGroup;
        setSelectedSecurityGroup(securityGroupId);  // 해당 보안 그룹을 선택하여 설정
      } else {
        setSelectedSecurityGroup(null);  // VPC에 해당하는 보안 그룹이 없으면 null로 설정
      }
    };

    const handleTaskChange = (e, type) => {
        const { value } = e.target;

        // 작업 선택 시 type에 따라 다르게 처리
        if (type === 'task') {
            // 선택된 작업을 selectedTask state에 저장
            setSelectedTask(value); // 선택된 작업 ID를 state에 설정
        } else if (type === 'region') {
            // 지역 선택 시 선택된 지역을 selectedRegion state에 저장
            setSelectedRegion(value); // 선택된 지역을 state에 설정
        }
    };

    const handleFetchTasks = () => {
        if (taskFormType === 'existingTask' && selectedRegion) {
            setTaskLoading(true); // 로딩 시작

            // API 요청을 보내기 전에 적절한 URL을 설정합니다.
            axios.get(`http://localhost:8080/dms/tasks?region=${selectedRegion}`)
                .then(response => {
                    const data = response.data;

                    // 빈 배열일 경우: 해당 지역에 작업이 없음
                    if (Array.isArray(data) && data.length === 0) {
                        alert('해당 지역에 작업이 없습니다.');
                        setTasks([]); // tasks 배열 초기화
                        setArns([]); // arns 배열 초기화
                    } else if (Array.isArray(data)) {
                        const taskIdentifiers = data.map(task => task.taskIdentifier);
                        const taskArns = data.map(task => task.arn);
                        setExistingTasks(taskIdentifiers); // taskIdentifier 배열 업데이트
                        console.log(existingTasks);
                        setArns(taskArns); // arn 배열 업데이트
                    } else {
                        // 정상 값이 아니면 오류
                        alert('작업 데이터를 불러오는 데 오류가 발생했습니다.');
                    }
                })
                .catch(error => {
                    // 서버 요청이 실패하면 오류 메시지 표시
                    console.error(error);
                    alert('작업 데이터를 불러오는 데 오류가 발생했습니다.');
                })
                .finally(() => {
                    setTaskLoading(false); // 로딩 종료
                });
        }
    };

    const addSelectionRule = () => {
    const newRule = {
      id: Date.now(),
      schema: '%',
      sourceTableName: '',
      action: '포함',
      columnFilters: [],
    };

    // 상태를 안전하게 업데이트
    setSelectionRules((prevRules) => {
      if (!Array.isArray(prevRules)) return [newRule]; // prevRules가 배열이 아닐 경우 새로운 배열로 초기화
      return [...prevRules, newRule];
    });
  };
  // 선택 규칙 삭제
const removeSelectionRule = (idToRemove) => {
  setSelectionRules((prevRules) => {
    const updatedRules = prevRules.filter((rule) => rule.id !== idToRemove);  // 특정 rule 삭제

    if (updatedRules.length === 0) {
      setTransformRules([]);  // 선택 규칙이 다 삭제되면 변환 규칙도 삭제
    }

    return updatedRules;  // 업데이트된 배열 반환
  });
};

    const handleAddTransformRule = () => {
        setTransformRules((prevRules) => [
            ...prevRules,
            { ruleTarget: '스키마', sourceName: '', sourceTableName: '', action: '' },
        ]);
    };

  // 변환 규칙 값 변경
    const handleTransformRuleChange = (index, field, value) => {
        setTransformRules((prevRules) =>
            prevRules.map((rule, i) =>
                i === index ? { ...rule, [field]: value } : rule
            )
        );
    };

    const handleRemoveTransformRule = (index) => {
        setTransformRules((prevRules) => prevRules.filter((_, i) => i !== index));
    };

const updateSelectionRule = (ruleId, field, value) => {
  setSelectionRules((prevRules) =>
    prevRules.map((rule) =>
      rule.id === ruleId ? { ...rule, [field]: value } : rule  // rule.id로 정확히 필터링
    )
  );
};

const addColumnFilter = (ruleId) => {
  setSelectionRules((prevRules) =>
    prevRules.map((rule) => {
      if (rule.id === ruleId) {
        const newFilter = { columnName: '', condition: '', value: '' };
        return { ...rule, columnFilters: [...rule.columnFilters, newFilter] };
      }
      return rule;
    })
  );
};

  const updateColumnFilter = (ruleIndex, filterIndex, key, value) => {
    const updatedRules = [...selectionRules];
    updatedRules[ruleIndex].columnFilters[filterIndex][key] = value;
    setSelectionRules(updatedRules);
  };

const removeColumnFilter = (ruleId, index) => {
  setSelectionRules((prevRules) =>
    prevRules.map((rule) => {
      if (rule.id === ruleId) {
        const updatedFilters = rule.columnFilters.filter((_, idx) => idx !== index);
        return { ...rule, columnFilters: updatedFilters };
      }
      return rule;
    })
  );
};

  const addCondition = (ruleIndex, filterIndex) => {
    const updatedRules = [...selectionRules];
    updatedRules[ruleIndex].columnFilters[filterIndex].conditions.push({
      id: Date.now(),
      conditionType: 'equals', // 기본값
      value: '',
    });
    setSelectionRules(updatedRules);
  };

  const removeCondition = (ruleIndex, filterIndex, conditionId) => {
    const updatedRules = [...selectionRules];
    updatedRules[ruleIndex].columnFilters[filterIndex].conditions = updatedRules[ruleIndex].columnFilters[
      filterIndex
    ].conditions.filter((condition) => condition.id !== conditionId);
    setSelectionRules(updatedRules);
  };

  const updateCondition = (ruleIndex, filterIndex, conditionIndex, key, value) => {
    const updatedRules = [...selectionRules];
    updatedRules[ruleIndex].columnFilters[filterIndex].conditions[conditionIndex][key] = value;
    setSelectionRules(updatedRules);
  };

const handleColumnFilterChange = (ruleId, index, field, value) => {
  setSelectionRules((prevRules) =>
    prevRules.map((rule) => {
      if (rule.id === ruleId) {
        const updatedFilters = rule.columnFilters.map((filter, idx) =>
          idx === index ? { ...filter, [field]: value } : filter
        );
        return { ...rule, columnFilters: updatedFilters };
      }
      return rule;
    })
  );
};

  return (
     <>
       <h1 className="dms-title">DMS 작업 생성</h1>
       <div className="source-endpoint">
         <div className="source-flex">
           <h2 className="source-endpoint-title">소스 엔드포인트</h2>
           <div className="form-type-selector">
             <label>
               <input
                 type="radio"
                 name="formType"
                 value="new"
                 checked={formType === 'new'}
                 onChange={() => setFormType('new')}
               />
               새 엔드포인트
             </label>
             <label>
               <input
                 type="radio"
                 name="formType"
                 value="existing"
                 checked={formType === 'existing'}
                 onChange={() => setFormType('existing')}
               />
               기존 엔드포인트
             </label>
           </div>
         </div>

         {formType === 'new' && (
           <div className="source-endpoint-form">
             <form onSubmit={(e) => e.preventDefault()}>
             <div className="source-1">
                 {/* 엔드포인트 이름 */}
                 <div className="input-group">
                 <label htmlFor="sourceEndpointId">엔드포인트 이름</label>
                 <input
                   type="text"
                   id="sourceEndpointId"
                   placeholder="Source Endpoint ID"
                   value={sourceEndpointId}
                   onChange={(e) => setSourceEndpointId(e.target.value)}
                 />
                 </div>

                 {/* 서버 이름 */}
                 <div className="input-group">
                 <label htmlFor="sourceServerName">서버 이름(또는 IP 주소)</label>
                 <input
                   type="text"
                   id="sourceServerName"
                   placeholder="Source Server Name"
                   value={sourceServerName}
                   onChange={(e) => setSourceServerName(e.target.value)}
                 />
                 </div>
             </div>

             {/* 엔드포인트 유형 */}
             <div className="input-group">
             <label htmlFor="dbType">엔드포인트 유형</label>
             <select value={sourceEngine} onChange={(e) => setSourceEngine(e.target.value)} className="select-type" id="dbType">
                <option value="aurora">Amazon Aurora MySQL</option>
                <option value="aurora-postgresql">Amazon Aurora PostgreSQL</option>
                <option value="docdb">Amazon DocumentDB (with MongoDB compatibility)</option>
                <option value="s3">Amazon S3</option>
                <option value="mysql">MySQL</option>
                <option value="postgres">PostgreSQL</option>
                <option value="oracle">Oracle</option>
                <option value="sqlserver">Microsoft SQL Server</option>
                <option value="mariadb">MariaDB</option>
                <option value="mongodb">MongoDB</option>
                <option value="db2">IBM Db2 LUW</option>
                <option value="sybase">SAP Sybase ASE</option>
                <option value="azure-sql-db">Microsoft Azure SQL Database</option>
                <option value="azure-sql-managed-instance">Microsoft Azure SQL Managed Instance</option>
                <option value="gcp-cloud-sql-mysql">Google Cloud SQL for MySQL</option>
             </select>
             </div>

             <div className="source-2">
                 {/* 포트 번호 */}
                 <div className="input-group">
                  <label htmlFor="sourcePort">포트 번호</label>
                 <input
                   type="number"
                   id="sourcePort"
                   placeholder="Source Port"
                   value={sourcePort}
                   onChange={(e) => setSourcePort(e.target.value)}
                 />
                 </div>

                 {/* 사용자 이름 */}
                 <div className="input-group">
                 <label htmlFor="sourceUsername">사용자 이름</label>
                 <input
                   type="text"
                   id="sourceUsername"
                   placeholder="Source Username"
                   value={sourceUsername}
                   onChange={(e) => setSourceUsername(e.target.value)}
                 />
                 </div>

                 {/* 암호 */}
                 <div className="input-group">
                 <label htmlFor="sourcePassword">암호</label>
                 <input
                   type="password"
                   id="sourcePassword"
                   placeholder="Source Password"
                   value={sourcePassword}
                   onChange={(e) => setSourcePassword(e.target.value)}
                 />
                 </div>
             </div>
             {/* 태그 입력란: key, value */}
             <div className="tags-container">
                <Tags
                   type="source"
                   tags={tags}
                   handleTagChange={handleTagChange}
                   handleAddTag={handleAddTag}
                   handleRemoveTag={handleRemoveTag}
                />
             </div>
             </form>
           </div>
         )}

        {formType === 'existing' && (
          <div className="existing-endpoint">
            {/* 리전 선택 드롭박스 */}
            <div className="existing-flex">
            <label>
              <select
                value={selectedRegion}
                onChange={(e) => setSelectedRegion(e.target.value)}
              >
                <option value="us-east-1">US East (N. Virginia)</option>
                <option value="us-east-2">US East (Ohio)</option>
                <option value="us-west-1">US West (N. California)</option>
                <option value="us-west-2">US West (Oregon)</option>
                <option value="af-south-1">Africa (Cape Town)</option>
                <option value="ap-east-1">Asia Pacific (Hong Kong)</option>
                <option value="ap-south-1">Asia Pacific (Mumbai)</option>
                <option value="ap-northeast-1">Asia Pacific (Tokyo)</option>
                <option value="ap-northeast-2">Asia Pacific (Seoul)</option>
                <option value="ap-northeast-3">Asia Pacific (Osaka)</option>
                <option value="ap-southeast-1">Asia Pacific (Singapore)</option>
                <option value="ap-southeast-2">Asia Pacific (Sydney)</option>
                <option value="ca-central-1">Canada (Central)</option>
                <option value="eu-central-1">EU (Frankfurt)</option>
                <option value="eu-west-1">EU (Ireland)</option>
                <option value="eu-west-2">EU (London)</option>
                <option value="eu-west-3">EU (Paris)</option>
                <option value="eu-north-1">EU (Stockholm)</option>
                <option value="me-south-1">Middle East (Bahrain)</option>
                <option value="sa-east-1">South America (São Paulo)</option>
                <option value="us-gov-west-1">AWS GovCloud (US-West)</option>
                <option value="us-gov-east-1">AWS GovCloud (US-East)</option>
                <option value="cn-north-1">China (Beijing)</option>
                <option value="cn-northwest-1">China (Ningxia)</option>
                {/* 다른 리전 옵션 추가 */}
              </select>
            </label>

            {/* Fetch 버튼 */}
            <button className="existing-button" onClick={handleFetchEndpoints} disabled={loading}>
              불러오기
            </button>
            {loading && <span className="loading-spinner"></span>}
            </div>

            {/* 소스 엔드포인트 선택 드롭박스는 엔드포인트 목록을 fetch 한 후 표시 */}
            {selectedRegion && existingEndpoints.length > 0 && (
              <select
                value={selectedEndpointId}
                onChange={(e) => handleEndpointChange(e, 'source')}
              >
                <option value="" disabled>엔드포인트를 선택하세요</option>
                {existingEndpoints.map((endpoint, index) => (
                  <option key={index} value={endpoint.id}>
                    {endpoint.id}
                  </option>
                ))}
              </select>
            )}
          </div>
        )}
      </div>

       <div className="target-endpoint">
         <div className="target-flex">
           <h2 className="target-endpoint-title">타겟 엔드포인트</h2>
           <div className="form-type-selector">
             <label>
               <input
                 type="radio"
                 name="targetFormType"
                 value="newTarget"
                 checked={targetFormType === 'newTarget'}
                 onChange={() => setTargetFormType('newTarget')}
               />
               새 엔드포인트
             </label>
             <label>
               <input
                 type="radio"
                 name="targetFormType"
                 value="existingTarget"
                 checked={targetFormType === 'existingTarget'}
                 onChange={() => setTargetFormType('existingTarget')}
               />
               기존 엔드포인트
             </label>
           </div>
         </div>

         {targetFormType === 'newTarget' && (
           <div className="target-endpoint-form">
             <form onSubmit={(e) => e.preventDefault()}>
             <div className="target-1">
                 {/* 엔드포인트 이름 */}
                 <div className="input-group">
                 <label htmlFor="targetEndpointId">엔드포인트 이름</label>
                 <input
                   type="text"
                   id="targetEndpointId"
                   placeholder="Target Endpoint ID"
                   value={targetEndpointId}
                   onChange={(e) => setTargetEndpointId(e.target.value)}
                 />
                 </div>

                 {/* 서버 이름 */}
                 <div className="input-group">
                 <label htmlFor="targetServerName">서버 이름(또는 IP 주소)</label>
                 <input
                   type="text"
                   id="targetServerName"
                   placeholder="Target Server Name"
                   value={targetServerName}
                   onChange={(e) => setTargetServerName(e.target.value)}
                 />
                 </div>
             </div>

             {/* 엔드포인트 유형 */}
             <div className="input-group">
             <label htmlFor="dbType">엔드포인트 유형</label>
             <select value={targetEngine} onChange={(e) => setTargetEngine(e.target.value)} className="select-type" id="dbType">
                <option value="aurora">Amazon Aurora MySQL</option>
                <option value="aurora-postgresql">Amazon Aurora PostgreSQL</option>
                <option value="docdb">Amazon DocumentDB (with MongoDB compatibility)</option>
                <option value="s3">Amazon S3</option>
                <option value="mysql">MySQL</option>
                <option value="postgres">PostgreSQL</option>
                <option value="oracle">Oracle</option>
                <option value="sqlserver">Microsoft SQL Server</option>
                <option value="mariadb">MariaDB</option>
                <option value="mongodb">MongoDB</option>
                <option value="db2">IBM Db2 LUW</option>
                <option value="sybase">SAP Sybase ASE</option>
                <option value="azure-sql-db">Microsoft Azure SQL Database</option>
                <option value="azure-sql-managed-instance">Microsoft Azure SQL Managed Instance</option>
                <option value="gcp-cloud-sql-mysql">Google Cloud SQL for MySQL</option>
             </select>
             </div>

             <div className="target-2">
                 {/* 포트 번호 */}
                 <div className="input-group">
                  <label htmlFor="targetPort">포트 번호</label>
                 <input
                   type="number"
                   id="targetPort"
                   placeholder="Target Port"
                   value={targetPort}
                   onChange={(e) => setTargetPort(e.target.value)}
                 />
                 </div>

                 {/* 사용자 이름 */}
                 <div className="input-group">
                 <label htmlFor="targetUsername">사용자 이름</label>
                 <input
                   type="text"
                   id="targetUsername"
                   placeholder="Target Username"
                   value={targetUsername}
                   onChange={(e) => setTargetUsername(e.target.value)}
                 />
                 </div>

                 {/* 암호 */}
                 <div className="input-group">
                 <label htmlFor="targetPassword">암호</label>
                 <input
                   type="password"
                   id="targetPassword"
                   placeholder="Target Password"
                   value={targetPassword}
                   onChange={(e) => setTargetPassword(e.target.value)}
                 />
                 </div>
             </div>

             {/* 태그 입력란: key, value */}
             <div className="tags-container">
                <Tags
                  type="target"
                  tags={targetTags}
                  handleTagChange={handleTagChange}
                  handleAddTag={handleAddTag}
                  handleRemoveTag={handleRemoveTag}
                />
             </div>
             </form>
           </div>
         )}

        {targetFormType === 'existingTarget' && (
          <div className="existing-endpoint">
            {/* 리전 선택 드롭박스 */}
            <div className="existing-flex">
            <label>
              <select
                value={selectedTargetRegion}
                onChange={(e) => setSelectedTargetRegion(e.target.value)}
              >
                <option value="us-east-1">US East (N. Virginia)</option>
                <option value="us-east-2">US East (Ohio)</option>
                <option value="us-west-1">US West (N. California)</option>
                <option value="us-west-2">US West (Oregon)</option>
                <option value="af-south-1">Africa (Cape Town)</option>
                <option value="ap-east-1">Asia Pacific (Hong Kong)</option>
                <option value="ap-south-1">Asia Pacific (Mumbai)</option>
                <option value="ap-northeast-1">Asia Pacific (Tokyo)</option>
                <option value="ap-northeast-2">Asia Pacific (Seoul)</option>
                <option value="ap-northeast-3">Asia Pacific (Osaka)</option>
                <option value="ap-southeast-1">Asia Pacific (Singapore)</option>
                <option value="ap-southeast-2">Asia Pacific (Sydney)</option>
                <option value="ca-central-1">Canada (Central)</option>
                <option value="eu-central-1">EU (Frankfurt)</option>
                <option value="eu-west-1">EU (Ireland)</option>
                <option value="eu-west-2">EU (London)</option>
                <option value="eu-west-3">EU (Paris)</option>
                <option value="eu-north-1">EU (Stockholm)</option>
                <option value="me-south-1">Middle East (Bahrain)</option>
                <option value="sa-east-1">South America (São Paulo)</option>
                <option value="us-gov-west-1">AWS GovCloud (US-West)</option>
                <option value="us-gov-east-1">AWS GovCloud (US-East)</option>
                <option value="cn-north-1">China (Beijing)</option>
                <option value="cn-northwest-1">China (Ningxia)</option>
              </select>
            </label>

            {/* Fetch 버튼 */}
            <button className="existing-button" onClick={handleFetchTargetEndpoints} disabled={targetLoading}>
              불러오기
            </button>
            {targetLoading && <span className="loading-spinner"></span>}
            </div>

            {selectedTargetRegion && existingTargetEndpoints.length > 0 && (
              <select
                value={selectedTargetEndpointId}
                onChange={(e) => handleEndpointChange(e, 'target')}
              >
                <option value="" disabled>엔드포인트를 선택하세요</option>
                {existingTargetEndpoints.map((endpoint, index) => (
                  <option key={index} value={endpoint.id}>
                    {endpoint.id}
                  </option>
                ))}
              </select>
            )}
          </div>
        )}
      </div>
      <div className="replication-instance">
      <div className="ri-flex">
        <h2 className="ri-title">복제 인스턴스</h2>
        <div className="form-type-selector">
          <label>
            <input
              type="radio"
              name="RIType"
              value="newInstance"
              checked={RIFormType === 'newRI'}
              onChange={() => setRIFormType('newRI')}
            />
            새 복제 인스턴스
          </label>
          <label>
            <input
              type="radio"
              name="RIFormType"
              value="existingInstance"
              checked={RIFormType === 'existingRI'}
              onChange={() => setRIFormType('existingRI')}
            />
            기존 복제 인스턴스
          </label>
        </div>
        </div>

        {/* 새 리플리케이션 인스턴스 생성 폼 */}
        {RIFormType === 'newRI' && (
          <div className="RI-form">
          <form onSubmit={(e) => e.preventDefault()}>
          <div className="RI-1">
            <div className="input-group">
              <label htmlFor="replicationInstanceId">복제 인스턴스 이름</label>
              <input
                type="text"
                id="replicationInstanceId"
                placeholder="Enter instance name"
                value={replicationInstanceId}
                onChange={(e) => setReplicationInstanceId(e.target.value)}
              />
            </div>
            <div className="input-group">
              <label htmlFor="riDescription">설명</label>
              <input
                type="text"
                id="riDescription"
                placeholder="Enter Description"
                value={riDescription}
                onChange={(e) => setRiDescription(e.target.value)}
              />
            </div>
            </div>
            <div className="input-group">
              <label htmlFor="replicationInstanceClass">인스턴스 클래스</label>
              <select
                id="replicationInstanceClass"
                value={replicationInstanceClass}
                onChange={(e) => setReplicationInstanceClass(e.target.value)}
                className="select-type"
              >
                <option value="" disabled>Select instance class</option>
                <option value="dms.t2.micro">dms.t2.micro (Free Tier)</option>
                <option value="dms.t2.small">dms.t2.small</option>
                <option value="dms.t2.medium">dms.t2.medium</option>
                <option value="dms.t2.large">dms.t2.large</option>
                <option value="dms.t3.micro">dms.t3.micro</option>
                <option value="dms.t3.small">dms.t3.small</option>
                <option value="dms.t3.medium">dms.t3.medium</option>
                <option value="dms.t3.large">dms.t3.large</option>
                <option value="dms.c4.large">dms.c4.large</option>
                <option value="dms.c4.xlarge">dms.c4.xlarge</option>
                <option value="dms.c4.2xlarge">dms.c4.2xlarge</option>
                <option value="dms.c4.4xlarge">dms.c4.4xlarge</option>
                <option value="dms.c5.large">dms.c5.large</option>
                <option value="dms.c5.xlarge">dms.c5.xlarge</option>
                <option value="dms.c5.2xlarge">dms.c5.2xlarge</option>
                <option value="dms.c5.4xlarge">dms.c5.4xlarge</option>
                <option value="dms.c5.9xlarge">dms.c5.9xlarge</option>
                <option value="dms.c5.12xlarge">dms.c5.12xlarge</option>
                <option value="dms.c5.18xlarge">dms.c5.18xlarge</option>
                <option value="dms.c5.24xlarge">dms.c5.24xlarge</option>
                <option value="dms.c6i.large">dms.c6i.large</option>
                <option value="dms.c6i.xlarge">dms.c6i.xlarge</option>
                <option value="dms.c6i.2xlarge">dms.c6i.2xlarge</option>
                <option value="dms.c6i.4xlarge">dms.c6i.4xlarge</option>
                <option value="dms.c6i.8xlarge">dms.c6i.8xlarge</option>
                <option value="dms.c6i.12xlarge">dms.c6i.12xlarge</option>
                <option value="dms.c6i.16xlarge">dms.c6i.16xlarge</option>
                <option value="dms.c6i.24xlarge">dms.c6i.24xlarge</option>
                <option value="dms.c6i.32xlarge">dms.c6i.32xlarge</option>
                <option value="dms.r4.large">dms.r4.large</option>
                <option value="dms.r4.xlarge">dms.r4.xlarge</option>
                <option value="dms.r4.2xlarge">dms.r4.2xlarge</option>
                <option value="dms.r4.4xlarge">dms.r4.4xlarge</option>
                <option value="dms.r4.8xlarge">dms.r4.8xlarge</option>
                <option value="dms.r5.large">dms.r5.large</option>
                <option value="dms.r5.xlarge">dms.r5.xlarge</option>
                <option value="dms.r5.2xlarge">dms.r5.2xlarge</option>
                <option value="dms.r5.4xlarge">dms.r5.4xlarge</option>
                <option value="dms.r5.8xlarge">dms.r5.8xlarge</option>
                <option value="dms.r5.12xlarge">dms.r5.12xlarge</option>
                <option value="dms.r5.16xlarge">dms.r5.16xlarge</option>
                <option value="dms.r5.24xlarge">dms.r5.24xlarge</option>
                <option value="dms.r6i.large">dms.r6i.large</option>
                <option value="dms.r6i.xlarge">dms.r6i.xlarge</option>
                <option value="dms.r6i.2xlarge">dms.r6i.2xlarge</option>
                <option value="dms.r6i.4xlarge">dms.r6i.4xlarge</option>
                <option value="dms.r6i.8xlarge">dms.r6i.8xlarge</option>
                <option value="dms.r6i.12xlarge">dms.r6i.12xlarge</option>
                <option value="dms.r6i.16xlarge">dms.r6i.16xlarge</option>
                <option value="dms.r6i.24xlarge">dms.r6i.24xlarge</option>
                <option value="dms.r6i.32xlarge">dms.r6i.32xlarge</option>
              </select>
            </div>
            <div className="input-group">
              <label htmlFor="engineVersion">엔진 버전</label>
              <select
                id="engineVersion"
                value={replicationEngineVersion}
                onChange={(e) => setReplicationEngineVersion(e.target.value)}
                className="select-type"
              >
                <option value="" disabled>Select engine version</option>
                <option value="3.5.1">3.5.1</option>
                <option value="3.5.2">3.5.2</option>
                <option value="3.5.3">3.5.3</option>
                <option value="3.5.4">3.5.4</option>
              </select>
            </div>
            <div className="input-group">
              <label htmlFor="highAvailability">고가용성</label>
              <select
                id="highAvailability"
                value={highAvailability}
                onChange={(e) => setHighAvailability(e.target.value)}
                className="select-type"
              >
                <option value="">선택 안 함</option>
                <option value="true">프로덕션 워크로드(다중 AZ)</option>
                <option value="false">개발 또는 테스트 워크로드 (단일 AZ)</option>
              </select>
            </div>
            <div className="RI-2">
            <div className="input-group">
              <label htmlFor="storage">스토리지 (GB)</label>
              <input
                type="number"
                id="storage"
                placeholder="Storage size in GB"
                value={storage}
                onChange={(e) => setStorage(e.target.value)}
              />
            </div>
            <div className="input-group">
              <label>
                <input
                  type="checkbox"
                  checked={publicAccessible}
                  onChange={(e) => setPublicAccessible(e.target.checked)}
                  className="check-box"
                />
                퍼블릭 액세스 가능
              </label>
            </div>
            </div>
             <div className="input-group">
               <label htmlFor="vpc">VPC</label>
               <select
                 id="vpc"
                 value={selectedVpc}
                 onChange={handleVpcChange} // VPC 선택 시 보안 그룹을 fetch
                 className="select-type"
               >
                 <option value="">Select VPC</option>
                 {vpcs.length === 0 ? (
                   <option value="">Loading...</option>
                 ) : (
                   vpcs.map((vpc) => (
                     <option key={vpc.vpcId} value={vpc.vpcId}>
                       {vpc.vpcId} {/* vpcId만 표시 */}
                     </option>
                   ))
                 )}
               </select>
             </div>
            <div className="input-group">
              <label htmlFor="subnetGroup">Subnet Group</label>
              <select
                id="subnetGroup"
                value={selectedSubnetGroup}
                onClick={fetchSubnetGroups} // 드롭다운을 클릭할 때 서브넷 그룹 불러오기
                onChange={(e) => setSelectedSubnetGroup(e.target.value)}
                className="select-type"
              >
                {/* 서브넷 그룹이 로딩 중일 때 "Loading..." 또는 "Select Subnet Group"을 보여줌 */}
                {subnetGroups.length === 0 ? (
                  <option value="" disabled>
                    {selectedSubnetGroup ? "Loading..." : "Select Subnet Group"}
                  </option>
                ) : (
                  subnetGroups.map((subnetGroup) => (
                    <option key={subnetGroup} value={subnetGroup}>
                      {subnetGroup}
                    </option>
                  ))
                )}
              </select>
            </div>
             <div className="tags-container">
                <Tags
                   type="RI"
                   tags={RITags}
                   handleTagChange={handleTagChange}
                   handleAddTag={handleAddTag}
                   handleRemoveTag={handleRemoveTag}
                />
             </div>
          </form>
          </div>
        )}

        {/* 기존 리플리케이션 인스턴스 선택 */}
        {RIFormType === 'existingRI' && (
          <div className="existing-endpoint">
          <div className="existing-flex">
          <label>
              <select
                value={selectedRIRegion}
                onChange={(e) => setSelectedRIRegion(e.target.value)}
              >
                <option value="us-east-1">US East (N. Virginia)</option>
                <option value="us-east-2">US East (Ohio)</option>
                <option value="us-west-1">US West (N. California)</option>
                <option value="us-west-2">US West (Oregon)</option>
                <option value="af-south-1">Africa (Cape Town)</option>
                <option value="ap-east-1">Asia Pacific (Hong Kong)</option>
                <option value="ap-south-1">Asia Pacific (Mumbai)</option>
                <option value="ap-northeast-1">Asia Pacific (Tokyo)</option>
                <option value="ap-northeast-2">Asia Pacific (Seoul)</option>
                <option value="ap-northeast-3">Asia Pacific (Osaka)</option>
                <option value="ap-southeast-1">Asia Pacific (Singapore)</option>
                <option value="ap-southeast-2">Asia Pacific (Sydney)</option>
                <option value="ca-central-1">Canada (Central)</option>
                <option value="eu-central-1">EU (Frankfurt)</option>
                <option value="eu-west-1">EU (Ireland)</option>
                <option value="eu-west-2">EU (London)</option>
                <option value="eu-west-3">EU (Paris)</option>
                <option value="eu-north-1">EU (Stockholm)</option>
                <option value="me-south-1">Middle East (Bahrain)</option>
                <option value="sa-east-1">South America (São Paulo)</option>
                <option value="us-gov-west-1">AWS GovCloud (US-West)</option>
                <option value="us-gov-east-1">AWS GovCloud (US-East)</option>
                <option value="cn-north-1">China (Beijing)</option>
                <option value="cn-northwest-1">China (Ningxia)</option>
              </select>
              </label>
            <button
              type="button"
              className="existing-button"
              onClick={handleFetchRI}
              disabled={RILoading}
            >
              {RILoading ? 'Loading...' : '불러오기'}
            </button>
            {RILoading && <span className="loading-spinner"></span>}
            </div>
            {/* 기존 인스턴스 목록 표시 */}
            {RIs.length > 0 && (
                <select
                  id="existingInstances"
                  value={selectedRIId}
                  onChange={(e) => handleEndpointChange(e, "RI")}
                  className="select-type"
                >
                  <option value="" disabled>인스턴스를 선택하세요</option>
                  {RIs.map((instance) => (
                    <option key={instance.name} value={instance.name}>
                      {instance.name}
                    </option>
                  ))}
                </select>
            )}
          </div>
        )}
      </div>
      <div className="dms-task">
        <div className="task-flex">
          <h2 className="task-title">마이그레이션 태스크 생성</h2>
          <div className="form-type-selector">
            <label>
              <input
                type="radio"
                name="taskFormType"
                value="newTask"
                checked={taskFormType === 'newTask'}
                onChange={() => setTaskFormType('newTask')}
              />
              새 작업
            </label>
            <label>
              <input
                type="radio"
                name="taskFormType"
                value="existingTask"
                checked={taskFormType === 'existingTask'}
                onChange={() => setTaskFormType('existingTask')}
              />
              기존 작업
            </label>
          </div>
        </div>

        {taskFormType === 'newTask' && (
          <div className="task-form">
            <form onSubmit={(e) => e.preventDefault()}>
                {/* 작업 이름 */}
                <div className="input-group">
                  <label htmlFor="taskName">작업 이름</label>
                  <input
                    type="text"
                    id="taskName"
                    placeholder="Task Name"
                    value={taskName}
                    onChange={(e) => setTaskName(e.target.value)}
                  />
                </div>
                {/* 마이그레이션 유형 */}
                <div className="input-group">
                  <label>마이그레이션 유형</label>
                  <div className="radio">
                    <label>
                      <input
                        type="radio"
                        name="migrationType"
                        value="full-load"
                        checked={migrationType === 'full-load'}
                        onChange={(e) => setMigrationType(e.target.value)}
                      />
                      기존 데이터 마이그레이션
                    </label>
                    <label>
                      <input
                        type="radio"
                        name="migrationType"
                        value="full-load-and-cdc"
                        checked={migrationType === 'full-load-and-cdc'}
                        onChange={(e) => setMigrationType(e.target.value)}
                      />
                      기존 데이터 마이그레이션 및 지속적인 변경 사항 복제
                    </label>
                    <label>
                      <input
                        type="radio"
                        name="migrationType"
                        value="cdc"
                        checked={migrationType === 'cdc'}
                        onChange={(e) => setMigrationType(e.target.value)}
                      />
                      데이터 변경 사항만 복제
                    </label>
                  </div>
                {/* LOB 설정 */}
              </div>
              <div className="input-group">
                <label>대상 테이블 준비 모드</label>
                <div className="radio">
                  <label>
                    <input
                      type="radio"
                      name="targetTablePreparationMode"
                      value="DO_NOTHING"
                      checked={targetTablePreparationMode === 'DO_NOTHING'}
                      onChange={(e) => setTargetTablePreparationMode(e.target.value)}
                    />
                    아무 작업 안 함
                  </label>
                  <label>
                    <input
                      type="radio"
                      name="targetTablePreparationMode"
                      value="DROP_AND_CREATE"
                      checked={targetTablePreparationMode === 'DROP_AND_CREATE'}
                      onChange={(e) => setTargetTablePreparationMode(e.target.value)}
                    />
                    대상에서 테이블 삭제
                  </label>
                  <label>
                    <input
                      type="radio"
                      name="targetTablePreparationMode"
                      value="TRUNCATE"
                      checked={targetTablePreparationMode === 'TRUNCATE'}
                      onChange={(e) => setTargetTablePreparationMode(e.target.value)}
                    />
                    자르기
                  </label>
                </div>
              </div>
              {/* LOB 열 설정 */}
              <div className="input-group">
                <label>LOB 열 설정</label>
                <div className="radio">
                  <label>
                    <input
                      type="radio"
                      name="lobColumnSettings"
                      value="FULL_LOB_MODE"
                      checked={lobColumnSettings === 'FULL_LOB_MODE'}
                      onChange={(e) => setLobColumnSettings(e.target.value)}
                    />
                    전체 LOB 모드
                  </label>
                  <label>
                    <input
                      type="radio"
                      name="lobColumnSettings"
                      value="INLINE_LOB_MODE"
                      checked={lobColumnSettings === 'INLINE_LOB_MODE'}
                      onChange={(e) => setLobColumnSettings(e.target.value)}
                    />
                    제한적 LOB 모드
                  </label>
                  <label>
                    <input
                      type="radio"
                      name="lobColumnSettings"
                      value="LOB_DISABLED"
                      checked={lobColumnSettings === 'LOB_DISABLED'}
                      onChange={(e) => setLobColumnSettings(e.target.value)}
                    />
                    LOB 열 포함 안 함
                  </label>
                </div>
              </div>
              <div className="task-1">
                <div className="input-group">
                  <label htmlFor="maxLobSize">최대 LOB 크기</label>
                  <input
                    type="number"
                    id="maxLobSize"
                    placeholder="Max LOB Size"
                    value={maxLobSize}
                    onChange={(e) => setMaxLobSize(e.target.value)}
                  />
                </div>
                <div className="input-group">
                  <label>
                    태스크 로그 (CloudWatch Logs 켜기)
                    <input
                      type="checkbox"
                      checked={taskLogs}
                      onChange={(e) => setTaskLogs(e.target.checked)}
                      className="check-box"
                    />
                  </label>
                </div>
                </div>
                {/* 데이터 검증 */}
                <div className="input-group">
                  <label>데이터 검증</label>
                  <div className="radio">
                    <label>
                      <input
                        type="radio"
                        name="dataValidation"
                        value="OFF"
                        checked={dataValidation === 'OFF'}
                        onChange={(e) => setDataValidation('OFF')}
                      />
                      끄기
                    </label>
                    <label>
                      <input
                        type="radio"
                        name="dataValidation"
                        value="VALIDATE_WITH_MIGRATION"
                        checked={dataValidation === 'VALIDATE_WITH_MIGRATION'}
                        onChange={(e) => setDataValidation('VALIDATE_WITH_MIGRATION')}
                      />
                      데이터 마이그레이션을 통해 검증
                    </label>
                    <label>
                      <input
                        type="radio"
                        name="dataValidation"
                        value="VALIDATE_WITHOUT_MIGRATION"
                        checked={dataValidation === 'VALIDATE_WITHOUT_MIGRATION'}
                        onChange={(e) => setDataValidation('VALIDATE_WITHOUT_MIGRATION')}
                      />
                      데이터 마이그레이션 없이 검증
                    </label>
                  </div>
                </div>
              <div className="input-group">
              <label>테이블 매핑</label>
              <div className="mapping-1">
                <label>선택 규칙</label>
                <a href="#!" onClick={addSelectionRule} className="link-button">추가</a>
                </div>
                    <ul>
                      {selectionRules.map((rule, ruleIndex) => (
                        <li key={rule.id}>
                            <div className="mapping-3">
                            <label>{`선택 규칙 ${ruleIndex + 1}`}</label>
                            <a href="#!" onClick={() => removeSelectionRule(rule.id)} className="link-button">
                              삭제
                            </a>
                            </div>
                            <div className="mapping-4">
                            <div className="input-group">
                              <label htmlFor={`schema-${rule.id}`}>스키마</label>
                              <input
                                id={`schema-${rule.id}`}
                                type="text"
                                placeholder="스키마"
                                value={rule.schema}
                                onChange={(e) => updateSelectionRule(rule.id, 'schema', e.target.value)}
                              />
                              </div>
                              <div className="input-group">
                              <label htmlFor={`sourceTableName-${rule.id}`}>소스 테이블 이름</label>
                              <input
                                id={`sourceTableName-${rule.id}`}
                                type="text"
                                placeholder="소스 테이블 이름"
                                value={rule.sourceTableName}
                                onChange={(e) => updateSelectionRule(rule.id, 'sourceTableName', e.target.value)}
                              />
                              </div>
                              </div>

                              <div className="input-group">
                              <label htmlFor={`action-${rule.id}`}>작업</label>
                              <select
                                id={`action-${rule.id}`}
                                className="select-type2"
                                value={rule.action}
                                onChange={(e) => updateSelectionRule(rule.id, 'action', e.target.value)}
                              >
                                <option value="포함">포함</option>
                                <option value="제외">제외</option>
                              </select>
                              </div>

                          {rule.action === '포함' && (
                            <div className="column-filters">
                              <a href="#!" onClick={() => addColumnFilter(rule.id)} className="link-button">
                                열 필터 추가
                              </a>
                              {rule.columnFilters.map((filter, index) => (
                                <div key={index}>
                                <div className="mapping-3">
                                <label>{`열 필터 ${index + 1}`}</label>
                                <a href="#!" className="link-button" onClick={() => removeColumnFilter(rule.id, index)}>삭제</a>
                                </div>
                                <div className="input-group">
                                 <label>열 이름</label>
                                  <input
                                    type="text"
                                    placeholder="열 이름"
                                    value={filter.columnName}
                                    onChange={(e) =>
                                      handleColumnFilterChange(rule.id, index, 'columnName', e.target.value)
                                    }
                                  />
                                  </div>
                                  <div className="input-group">
                                  <label>조건</label>
                                  <input
                                    type="text"
                                    placeholder="조건"
                                    value={filter.condition}
                                    onChange={(e) =>
                                      handleColumnFilterChange(rule.id, index, 'condition', e.target.value)
                                    }
                                  />
                                  </div>
                                  <div className="input-group">
                                  <label>값</label>
                                  <input
                                    type="text"
                                    placeholder="값"
                                    value={filter.value}
                                    onChange={(e) =>
                                      handleColumnFilterChange(rule.id, index, 'value', e.target.value)
                                    }
                                  />
                                  </div>
                                </div>
                              ))}
                            </div>
                          )}
                        </li>
                      ))}
                    </ul>
                </div>
                {selectionRules.length > 0 && (
                    <div className="mapping-2">
                        <label className="transformation">변환 규칙</label>
                        <a href="#!" onClick={handleAddTransformRule} className="link-button">
                            추가
                        </a>
                    </div>
                )}

                {/* 변환 규칙 입력 폼 */}
                {transformRules.length > 0 && (
                    <div className="transform-rules">
                        {transformRules.map((rule, index) => (
                            <div key={index} className="transform-rule">
                                <div className="input-group">
                                    <div className="mapping-3">
                                        <label>{`변환 규칙 ${index + 1}`}</label>
                                        <a
                                            href="#!"
                                            onClick={() => handleRemoveTransformRule(index)}
                                            className="link-button"
                                            aria-label="Remove transform rule"
                                        >
                                            삭제
                                        </a>
                                    </div>
                                        <label htmlFor={`ruleTarget-${index}`}>규칙 대상</label>
                                        <select
                                            id={`ruleTarget-${index}`}
                                            value={rule.ruleTarget}
                                            onChange={(e) =>
                                                handleTransformRuleChange(index, 'ruleTarget', e.target.value)
                                            }
                                            className="select-type2"
                                        >
                                            <option value="schema">스키마</option>
                                            <option value="table">테이블</option>
                                            <option value="column">열</option>
                                        </select>
                                    </div>

                                    {/* 소스 이름 */}
                                    <div className="input-group">
                                        <label htmlFor={`sourceName-${index}`}>소스 이름</label>
                                        <input
                                            id={`sourceName-${index}`}
                                            type="text"
                                            value={rule.sourceName}
                                        onChange={(e) =>
                                            handleTransformRuleChange(index, 'sourceName', e.target.value)
                                        }
                                        placeholder="소스 이름 입력"
                                    />
                                </div>

                                {/* 소스 테이블 이름 */}
                                {(rule.ruleTarget === 'table' || rule.ruleTarget === 'column') && (
                                    <div className="input-group">
                                        <label htmlFor={`sourceTableName-${index}`}>소스 테이블 이름</label>
                                        <input
                                            id={`sourceTableName-${index}`}
                                            type="text"
                                            value={rule.sourceTableName}
                                            onChange={(e) =>
                                                handleTransformRuleChange(index, 'sourceTableName', e.target.value)
                                            }
                                            placeholder="소스 테이블 이름 입력"
                                        />
                                    </div>
                                )}

                                {/* 작업 */}
                                <div className="input-group">
                                    <label htmlFor={`action-${index}`}>작업</label>
                                    <select
                                        id={`action-${index}`}
                                        value={rule.action}
                                        onChange={(e) =>
                                            handleTransformRuleChange(index, 'action', e.target.value)
                                        }
                                        className="select-type"
                                    >
                                        {rule.ruleTarget === 'schema' && (
                                            <>
                                                <option value="rename">새 이름</option>
                                                <option value="lowercase">소문자로 변경</option>
                                                <option value="uppercase">대문자로 변경</option>
                                                <option value="add_prefix">접두사 추가</option>
                                                <option value="remove_prefix">접두사 제거</option>
                                                <option value="replace_prefix">접두사 바꾸기</option>
                                                <option value="add_suffix">접미사 추가</option>
                                                <option value="remove_suffix">접미사 제거</option>
                                                <option value="replace_suffix">접미사 바꾸기</option>
                                            </>
                                        )}
                                        {rule.ruleTarget === 'table' && (
                                            <>
                                                <option value="rename">새 이름</option>
                                                <option value="lowercase">소문자로 변경</option>
                                                <option value="uppercase">대문자로 변경</option>
                                                <option value="add_prefix">접두사 추가</option>
                                                <option value="remove_prefix">접두사 제거</option>
                                                <option value="replace_prefix">접두사 바꾸기</option>
                                                <option value="add_suffix">접미사 추가</option>
                                                <option value="remove_suffix">접미사 제거</option>
                                                <option value="replace_suffix">접미사 바꾸기</option>
                                                <option value="define_primary_key">프라이머리 키 정의</option>
                                            </>
                                        )}
                                        {rule.ruleTarget === 'column' && (
                                            <>
                                                <option value="rename_column">새 이름</option>
                                                <option value="drop_column">열 제거</option>
                                                <option value="include_column">열 포함</option>
                                                <option value="lowercase">소문자로 변경</option>
                                                <option value="uppercase">대문자로 변경</option>
                                                <option value="add_prefix">접두사 추가</option>
                                                <option value="remove_prefix">접두사 제거</option>
                                                <option value="replace_prefix">접두사 바꾸기</option>
                                                <option value="add_suffix">접미사 추가</option>
                                                <option value="remove_suffix">접미사 제거</option>
                                                <option value="replace_suffix">접미사 바꾸기</option>
                                                <option value="change_data_type">데이터 형식 변경</option>
                                                <option value="add_column">열 추가</option>
                                                <option value="add_image_column_before">이미지 열 앞에 추가</option>
                                            </>
                                        )}
                                    </select>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
                <div className="input-group">
                <label>마이그레이션 태스크 시작 구성</label>
                <div className="radio">
                  <label>
                    <input
                      type="radio"
                      name="startTaskOnCreation"
                      value="true"
                      checked={startTaskOnCreation === 'true'}
                      onChange={(e) => setStartTaskOnCreation(e.target.value)}
                    />
                    마이그레이션 태스크 시작
                  </label>
                  <label>
                    <input
                      type="radio"
                      name="startTaskOnCreation"
                      value="false"
                      checked={startTaskOnCreation === 'false'}
                      onChange={(e) => setStartTaskOnCreation(e.target.value)}
                    />
                    나중에 수동으로 시작
                  </label>
                </div>
              </div>
              {/* 태그 입력란 */}
              <div className="tags-container">
                <Tags
                  type="task"
                  tags={dmsTaskTags}
                  handleTagChange={handleTagChange}
                  handleAddTag={handleAddTag}
                  handleRemoveTag={handleRemoveTag}
                />
              </div>
            </form>
          </div>
        )}
         {taskFormType === 'existingTask' && (
             <div className="existing-endpoint">
                 <div className="existing-flex">
                     {/* 지역 선택 */}
                     <label>
                         <select
                             value={selectedRegion}
                             onChange={(e) => setSelectedRegion(e.target.value)}
                         >
                             <option value="us-east-1">US East (N. Virginia)</option>
                             <option value="us-east-2">US East (Ohio)</option>
                             <option value="us-west-1">US West (N. California)</option>
                             <option value="us-west-2">US West (Oregon)</option>
                             <option value="af-south-1">Africa (Cape Town)</option>
                             <option value="ap-east-1">Asia Pacific (Hong Kong)</option>
                             <option value="ap-south-1">Asia Pacific (Mumbai)</option>
                             <option value="ap-northeast-1">Asia Pacific (Tokyo)</option>
                             <option value="ap-northeast-2">Asia Pacific (Seoul)</option>
                             <option value="ap-northeast-3">Asia Pacific (Osaka)</option>
                             <option value="ap-southeast-1">Asia Pacific (Singapore)</option>
                             <option value="ap-southeast-2">Asia Pacific (Sydney)</option>
                             <option value="ca-central-1">Canada (Central)</option>
                             <option value="eu-central-1">EU (Frankfurt)</option>
                             <option value="eu-west-1">EU (Ireland)</option>
                             <option value="eu-west-2">EU (London)</option>
                             <option value="eu-west-3">EU (Paris)</option>
                             <option value="eu-north-1">EU (Stockholm)</option>
                             <option value="me-south-1">Middle East (Bahrain)</option>
                             <option value="sa-east-1">South America (São Paulo)</option>
                             <option value="us-gov-west-1">AWS GovCloud (US-West)</option>
                             <option value="us-gov-east-1">AWS GovCloud (US-East)</option>
                             <option value="cn-north-1">China (Beijing)</option>
                             <option value="cn-northwest-1">China (Ningxia)</option>
                         </select>
                     </label>

                     <button
                         type="button"
                         className="existing-button"
                         onClick={handleFetchTasks}
                         disabled={taskLoading}
                     >
                         {taskLoading ? 'Loading...' : '불러오기'}
                     </button>
                     {taskLoading && <span className="loading-spinner"></span>}
                 </div>
                 {existingTasks.length > 0 && (
                     <select
                         id="existingTasks"
                         value={selectedTaskId}
                         onChange={(e) => handleEndpointChange(e, 'task')}
                         className="select-type"
                     >
                         <option value="" disabled>작업을 선택하세요</option>
                         {existingTasks.map((task) => (
                             <option key={task} value={task}>
                                 {task} {/* taskIdentifier를 화면에 표시 */}
                             </option>
                         ))}
                     </select>
                 )}
             </div>
         )}
      </div>
         <div className="spinner">
          {/* 소스 엔드포인트 생성 폼 */}
          <button
            className="source-create"
            onClick={handleCreateSourceEndpoint}
            disabled={createLoading} // 로딩 중일 때 버튼 비활성화
          >
            {createLoading ? (
              <>
                생성 중...
              </>
            ) : (
              '생성'
            )}
          </button>

          {/* 로딩 스피너 */}
          {createLoading && <div className="loading-spinner"></div>}
        </div>
    </>
  );
}

export default Dms;
