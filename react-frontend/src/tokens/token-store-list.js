import React, {useEffect, useState} from "react";
import {TokenStoreClient} from "./token-store-client";
import {Alert, Table} from "react-bootstrap";
import {CheckLg, Eye, EyeFill, PencilSquare, PlusLg, Save, Trash, XLg} from "react-bootstrap-icons";
import {Optional} from "../services/utils";
import {GoFlame} from "react-icons/go";

export function TokenStoreList(props)
{
  const [errors, setErrors] = useState({});
  const [loadedOnce, setloadedOnce] = useState(false);
  const [totalResults, setTotalResults] = useState(0);
  const [tokenStoreList, setTokenStoreList] = useState([]);
  
  function addNewTokenStores(tokenStoreArray)
  {
    let newTokenStores = [...tokenStoreList].concat(tokenStoreArray);
    newTokenStores.sort((c1, c2) => c1.name.localeCompare(c2.name));
    setTotalResults(totalResults + tokenStoreArray.length);
    setTokenStoreList(newTokenStores);
  }
  
  function updateTokenStore(oldtokenStore, newTokenStore)
  {
    let copiedTokenStores = [...tokenStoreList];
    let indexOf = copiedTokenStores.indexOf(oldtokenStore);
    copiedTokenStores.splice(indexOf, 1, newTokenStore);
    copiedTokenStores.sort((c1, c2) => c1.name.localeCompare(c2.name));
    setTokenStoreList(copiedTokenStores);
  }
  
  function removeTokenStore(tokenStore)
  {
    let copiedTokenStores = [...tokenStoreList];
    let indexOf = copiedTokenStores.indexOf(tokenStore);
    copiedTokenStores.splice(indexOf, 1);
    setTotalResults(totalResults - 1);
    setTokenStoreList(copiedTokenStores);
  }
  
  useEffect(() =>
            {
              let searchRequest = {
                startIndex: tokenStoreList.length,
                sortBy: "name"
              };
    
              function onSuccess(listResponse)
              {
                setTotalResults(listResponse.totalResults);
                let newResources = listResponse.Resources || [];
                addNewTokenStores([...newResources]);
                setloadedOnce(true);
              }
    
              function onError(errorResponse)
              {
                setErrors(errorResponse);
              }
    
              if ((totalResults === 0 && !loadedOnce) || tokenStoreList.length < totalResults)
              {
                new TokenStoreClient().listTokenStores(searchRequest, onSuccess, onError);
              }
            }, [tokenStoreList]);
  
  return <React.Fragment>
    {
      errors && errors.length > 0 &&
      <Alert variant={"danger"}>
        <GoFlame /> {errors.details}
      </Alert>
    }
    <TokenStoreTable category={props.category}
                     tokenStoreList={tokenStoreList}
                     addNewTokenStores={addNewTokenStores}
                     updateTokenStore={updateTokenStore}
                     removeTokenStore={removeTokenStore} />
  </React.Fragment>;
}

function TokenStoreTable(props)
{
  
  const [error, setError] = useState();
  const [addNew, setAddNew] = useState(false);
  
  function deleteTokenStore(tokenStore)
  {
    function onSuccess()
    {
      props.removeTokenStore(tokenStore);
    }
    
    function onError(errorResponse)
    {
      setError(errorResponse);
    }
    
    new TokenStoreClient().deleteTokenStore(tokenStore, onSuccess, onError);
  }
  
  return <React.Fragment>
    {
      error && error.length > 0 &&
      <Alert variant={"danger"}>
        <GoFlame /> {error.detail}
      </Alert>
    }
    <Table striped bordered hover size="sm" variant={"dark"}>
      <thead>
        <tr>
          <th className={"token-store-id-column"}>id</th>
          <th className={"token-store-name-column"}>name</th>
          <th>token</th>
          <th className={"timestamp-column"}>created</th>
          <th className={"timestamp-column"}>lastModified</th>
          <th className={"icon-column"}><PlusLg className={"icon"} onClick={() => setAddNew(true)} /></th>
        </tr>
      </thead>
      <tbody>
        {
          addNew &&
          <TokenStoreRow tokenStore={{
            id: 0,
            categoryId: parseInt(props.category.id)
          }}
                         editMode={true}
                         addNewTokenStores={resourceArray =>
                         {
                           setAddNew(false);
                           props.addNewTokenStores(resourceArray);
                         }}
                         removeTokenStore={() => setAddNew(false)} />
        }
        {
          props.tokenStoreList.map(tokenStore => <TokenStoreRow key={tokenStore.id}
                                                                tokenStore={tokenStore}
                                                                addNewTokenStores={props.addNewTokenStores}
                                                                updateTokenStore={props.updateTokenStore}
                                                                removeTokenStore={deleteTokenStore} />)
        }
      </tbody>
    </Table>
  </React.Fragment>;
}

function TokenStoreRow(props)
{
  
  const [error, setError] = useState();
  const [deleteMode, setDeleteMode] = useState(false);
  const [editMode, setEditMode] = useState(props.editMode);
  const [viewTokenMode, setViewTokenMode] = useState(false);
  
  const [token, setToken] = useState(props.tokenStore.token || "");
  const [tokenName, setTokenName] = useState(props.tokenStore.name || "");
  
  function onError(errorResponse)
  {
    setError(errorResponse);
  }
  
  function createNewTokenStore()
  {
    function onSuccess(resource)
    {
      setEditMode(false);
      props.addNewTokenStores([resource]);
    }
    
    setError(null);
    new TokenStoreClient().createTokenStore(tokenName, token, props.tokenStore.categoryId, onSuccess, onError);
  }
  
  function updateTokenStore()
  {
    function onSuccess(resource)
    {
      setEditMode(false);
      props.updateTokenStore(props.tokenStore, resource);
    }
    
    setError(null);
    if (props.tokenStore.name !== tokenName || props.tokenStore.token !== token)
    {
      new TokenStoreClient().updateTokenStore(props.tokenStore.id,
                                              tokenName,
                                              token,
                                              props.tokenStore.categoryId,
                                              onSuccess,
                                              onError);
    }
    else
    {
      setEditMode(false);
    }
  }
  
  function resetChanges()
  {
    setTokenName(props.tokenStore.name);
    setToken(props.tokenStore.token);
    setEditMode(false);
  }
  
  return <React.Fragment>
    <tr>
      <td>{props.tokenStore.id}</td>
      <td>
        {
          !editMode &&
          tokenName
        }
        {
          editMode &&
          <input type={"text"} value={tokenName} onChange={e => setTokenName(e.target.value)} />
        }
      </td>
      <td className={"overflow-column"}>
        {
          !editMode &&
          <React.Fragment>
            {
              !viewTokenMode &&
              token
            }
            {
              viewTokenMode &&
              <pre className={"token-overview"}>
                {token}
              </pre>
            }
            {
              !viewTokenMode &&
              <Eye className={"right-floating-icon eye-icon"} onClick={() => setViewTokenMode(!viewTokenMode)} />
            }
            {
              viewTokenMode &&
              <EyeFill className={"right-floating-icon eye-icon"} onClick={() => setViewTokenMode(!viewTokenMode)} />
            }
          </React.Fragment>
        }
        {
          editMode &&
          <textarea value={token} onChange={e => setToken(e.target.value)} className={"token-area"} wrap="off" />
        }
      </td>
      <td>
        {new Optional(props.tokenStore.meta).map(val => val.created)
                                            .map(val => new Date(val).toLocaleString())
                                            .orElse(null)}
      </td>
      <td>
        {new Optional(props.tokenStore.meta).map(val => val.lastModified)
                                            .map(val => new Date(val).toLocaleString())
                                            .orElse(null)}
      </td>
      <td>
        {
          !deleteMode &&
          <React.Fragment>
            {
              !editMode &&
              <PencilSquare onClick={() => setEditMode(!editMode)} className={"listed-icon icon"} />
            }
            {
              editMode &&
              <React.Fragment>
                <Save className={"listed-icon icon"}
                      onClick={() =>
                      {
                        if (props.tokenStore.id === 0)
                        {
                          createNewTokenStore();
                        }
                        else
                        {
                          updateTokenStore();
                        }
                      }} />
                <XLg className={"listed-icon icon"}
                     onClick={() => resetChanges()} />
              </React.Fragment>
            }
            <Trash onClick={() => setDeleteMode(true)} className={"icon"} />
          </React.Fragment>
        }
        {
          deleteMode &&
          <span className={"list-delete-insertion"}>
            sure? <CheckLg className={"listed-icon icon"}
                           onClick={() =>
                           {
                             setDeleteMode(false);
                             props.removeTokenStore(props.tokenStore);
                           }} />
            <XLg className={"listed-icon icon"}
                 onClick={() => setDeleteMode(false)} />
          </span>
        }
      </td>
    </tr>
  
  </React.Fragment>;
}
