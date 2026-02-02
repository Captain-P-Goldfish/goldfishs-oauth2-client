import React, {useContext, useEffect, useState} from "react";
import {TokenStoreClient} from "./token-store-client";
import { Alert, Button, Popover, Table, Tooltip } from "react-bootstrap";
import {CheckLg, PencilSquare, PlusLg, Save, Trash, XLg} from "react-bootstrap-icons";
import {Optional} from "../services/utils";
import {GoFlame} from "react-icons/go";
import {ScimServiceProviderContext} from "../app";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import { AiTwotoneCopy } from "react-icons/ai";

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
    props.setCategoryEntires(props.category, totalResults + tokenStoreArray.length);
  }

  function updateTokenStore(oldtokenStore, newTokenStore)
  {
    let copiedTokenStores = [...tokenStoreList];
    let indexOf = copiedTokenStores.indexOf(oldtokenStore);
    copiedTokenStores.splice(indexOf, 1, newTokenStore);
    copiedTokenStores.sort((c1, c2) => c1.name.localeCompare(c2.name));
    setTokenStoreList(copiedTokenStores);
  }

  function removeTokenStore(tokenStoreArray)
  {
    let copiedTokenStores = [...tokenStoreList];
    tokenStoreArray.forEach(tokenStore =>
    {
      let indexOf = copiedTokenStores.indexOf(tokenStore);
      copiedTokenStores.splice(indexOf, 1);
    });
    setTotalResults(totalResults - tokenStoreArray.length);
    setTokenStoreList(copiedTokenStores);
    props.setCategoryEntires(props.category, totalResults - tokenStoreArray.length);
  }

  function toggleSingleCeckbox(checked, tokenStore)
  {
    tokenStore.checked = checked;
    let copiedTokenStores = [...tokenStoreList];
    let indexOf = copiedTokenStores.indexOf(tokenStore);
    copiedTokenStores.splice(indexOf, 1, tokenStore);
    setTokenStoreList(copiedTokenStores);
  }

  function toggleAllCheckboxes(checked)
  {
    let copiedTokenStores = [...tokenStoreList];
    copiedTokenStores.forEach(tokenStore =>
    {
      tokenStore.checked = checked;
    });
    setTokenStoreList(copiedTokenStores);
  }

  useEffect(() =>
  {
    setloadedOnce(false);
    setTotalResults(0);
    setTokenStoreList([]);
  }, [props.filter]);

  useEffect(() =>
  {
    let searchRequest = {
      startIndex: tokenStoreList.length,
      filter: new Optional(props.filter).map(v => v.trim())
        .map(v => v.length === 0 ? undefined : v)
        .map(v => "token co \"" + v + "\" and categoryId eq " + props.category.id)
        .orElse("categoryId eq " + props.category.id),
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
        <GoFlame/> {errors.details}
      </Alert>
    }
    <TokenStoreTable category={props.category}
                     tokenStoreList={tokenStoreList}
                     addNewTokenStores={addNewTokenStores}
                     updateTokenStore={updateTokenStore}
                     removeTokenStore={removeTokenStore}
                     toggleSingleCeckbox={toggleSingleCeckbox}
                     toggleAllCheckboxes={toggleAllCheckboxes}/>
  </React.Fragment>;
}

function TokenStoreTable(props)
{

  const [error, setError] = useState();
  const [bulkDeleteMode, setBulkDeleteMode] = useState(false);
  const [addNew, setAddNew] = useState(false);

  const serviceProviderContext = useContext(ScimServiceProviderContext);

  function deleteTokenStore(tokenStoreArray)
  {
    function onSuccess()
    {
      props.removeTokenStore(tokenStoreArray);
    }

    function onError(errorResponse)
    {
      setError(errorResponse);
    }

    new TokenStoreClient().deleteTokenStore(tokenStoreArray[0], onSuccess, onError);
  }

  function bulkDeleteTokenStore()
  {
    let tokenStoreArray = props.tokenStoreList.filter(tokenStore => tokenStore.checked);

    function onSuccess(successfulDeleteIds, failedDeleteIds)
    {
      let deletedTokenStores = tokenStoreArray.filter(tokenStore => successfulDeleteIds.includes(tokenStore.id));
      props.removeTokenStore(deletedTokenStores);
      setBulkDeleteMode(false);
    }

    function onError(errorResponse)
    {
      setError(errorResponse);
      setBulkDeleteMode(false);
    }

    new TokenStoreClient().bulkDeleteTokenStores(tokenStoreArray,
      serviceProviderContext.bulk.maxOperations,
      onSuccess,
      onError);
  }

  return <React.Fragment>
    {
      error && error.length > 0 &&
      <Alert variant={"danger"}>
        <GoFlame/> {error.detail}
      </Alert>
    }
    <Table striped bordered hover size="sm" variant={"dark"}>
      <thead>
        <tr>
          <th className={"checkbox-column"}>
            <input type={"checkbox"}
                   onChange={e => props.toggleAllCheckboxes(e.target.checked)}/>
          </th>
          <th className={"token-store-id-column"}>id</th>
          <th className={"token-store-name-column"}>name</th>
          <th>token</th>
          <th className={"timestamp-column"}>timestamps</th>
          <th className={"icon-column"}>
            {
              !bulkDeleteMode &&
              <React.Fragment>
                <PlusLg className={"listed-icon icon"} onClick={() => setAddNew(true)}/>
                <Trash className={"icon"} onClick={() => setBulkDeleteMode(true)}/>
              </React.Fragment>
            }
            {
              bulkDeleteMode &&
              <span className={"list-delete-insertion"}>
                sure? <CheckLg className={"listed-icon icon"} onClick={() => bulkDeleteTokenStore()}/>
                <XLg className={"icon"} onClick={() => setBulkDeleteMode(false)}/>
              </span>
            }
          </th>
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
                         removeTokenStore={() => setAddNew(false)}/>
        }
        {
          props.tokenStoreList.map(tokenStore => <TokenStoreRow key={tokenStore.id}
                                                                tokenStore={tokenStore}
                                                                toggleSingleCeckbox={props.toggleSingleCeckbox}
                                                                addNewTokenStores={props.addNewTokenStores}
                                                                updateTokenStore={props.updateTokenStore}
                                                                removeTokenStore={deleteTokenStore}/>)
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
  const [wasCopied, setWasCopied] = useState(false);

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
    } else
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
      <td>
        <input type={"checkbox"} checked={props.tokenStore.checked || false}
               onChange={e => props.toggleSingleCeckbox(e.target.checked, props.tokenStore)}/>
      </td>
      <td>{props.tokenStore.id}</td>
      <td>
        {
          !editMode &&
          tokenName
        }
        {
          editMode &&
          <input type={"text"} value={tokenName} onChange={e => setTokenName(e.target.value)}/>
        }
      </td>
      {
        !editMode &&
        <React.Fragment>
          <OverlayTrigger placement="bottom"
                          trigger={"click"}
                          delay={{ show: 0, hide: 400 }}
                          overlay={<Popover className={"token-popover"}>
                            <Popover.Header as="h3" style={{ height: "35px" }}>
                              {tokenName}
                              <OverlayTrigger
                                placement="top"
                                delay={{ show: 250, hide: 400 }}
                                overlay={<Tooltip>
                                  {
                                    wasCopied &&
                                    "Copy successful"
                                  }
                                  {
                                    !wasCopied &&
                                    "Copy to Clipboard"
                                  }
                                </Tooltip>}
                              >
                                <Button className={"m-0 p-0 bg-transparent float-start border-0 me-2 text-black"}>
                                  <AiTwotoneCopy style={{ height: "25px", width: "20px" }}
                                                 onClick={() => {
                                                   navigator.clipboard.writeText(token);
                                                   setWasCopied(true);
                                                   setTimeout(function(){
                                                     setWasCopied(() => false);
                                                   }, 1500)
                                                 }}/>
                                </Button>
                              </OverlayTrigger>
                            </Popover.Header>
                            <Popover.Body>
                              <pre className={"text-start text-primary"}>
                                {token}
                              </pre>
                            </Popover.Body>
                          </Popover>}
          >
            <td className={"overflow-column"}>
              <article className={"token-overview-short"}>{token}</article>
            </td>
          </OverlayTrigger>
        </React.Fragment>
      }
      {
        editMode &&
        <td className={"overflow-column"}>
          <textarea value={token} onChange={e => setToken(e.target.value)} className={"token-area"} wrap="off"/>
        </td>
      }
      <td>
        <span>
          created: {new Optional(props.tokenStore.meta).map(val => val.created)
                                                       .map(val => new Date(val).toLocaleString())
                                                       .orElse(null)}
        </span>
        <br/>
        <span>
          modified: {new Optional(props.tokenStore.meta).map(val => val.lastModified)
                                                        .map(val => new Date(val).toLocaleString())
          .orElse(null)}
        </span>
      </td>
      <td>
        {
          !deleteMode &&
          <React.Fragment>
            {
              !editMode &&
              <PencilSquare onClick={() => setEditMode(!editMode)} className={"listed-icon icon"}/>
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
                        } else
                        {
                          updateTokenStore();
                        }
                      }}/>
                <XLg className={"listed-icon icon"}
                     onClick={() => resetChanges()}/>
              </React.Fragment>
            }
            <Trash onClick={() => setDeleteMode(true)} className={"icon"}/>
          </React.Fragment>
        }
        {
          deleteMode &&
          <span className={"list-delete-insertion"}>
            sure? <CheckLg className={"listed-icon icon"}
                           onClick={() =>
                           {
                             setDeleteMode(false);
                             props.removeTokenStore([props.tokenStore]);
                           }}/>
            <XLg className={"listed-icon icon"}
                 onClick={() => setDeleteMode(false)}/>
          </span>
        }
      </td>
    </tr>

  </React.Fragment>;
}
