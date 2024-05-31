import React, {useContext, useEffect, useRef, useState} from 'react';
import {useUniqueArray} from "../services/array-utils";
import {ScimClient2} from "../scim/scim-client-2";
import {HTTP_REQUEST_GROUPS_ENDPOINT} from "../scim/scim-constants";
import {RequestGroupUpdateContext} from "./request-group-provider";
import {Form, ListGroup, OverlayTrigger, Popover} from "react-bootstrap";
import {PencilSquare, PlusLg, Save, Trash, XLg} from "react-bootstrap-icons";
import {Optional, useScimErrorResponse} from "../services/utils";
import {GoFlame} from "react-icons/go";
import * as lodash from "lodash";
import Button from "react-bootstrap/Button";

export function RequestGroupMenuBar()
{

  const [errorResponse, setErrorResponse] = useState();
  const [requestGroups, setRequestGroups, isInsertable,
    addRequestGroup, updateRequestGroup, removeRequestGroup] =
    useUniqueArray([], requestGroup => requestGroup.name);
  const updateRequestGroupContext = useContext(RequestGroupUpdateContext);
  const [addNewEntry, setAddNewEntry] = useState(false);
  const newRequestGroupName = useRef();

  useEffect(() =>
  {
    let scimClient = new ScimClient2();
    scimClient.listResources({
      resourcePath: HTTP_REQUEST_GROUPS_ENDPOINT,
      onSuccess: listResponse => setRequestGroups(
        [...listResponse.Resources || []]),
      onError: errorResponse => setErrorResponse(errorResponse)
    });
  }, []);

  useEffect(() =>
  {
    if (addNewEntry === true)
    {
      newRequestGroupName.current.focus();
    }
  }, [addNewEntry]);

  function saveNewRequestGroup()
  {
    let newRequestGroup = {name: newRequestGroupName.current.value};
    let errorMessage = isInsertable(newRequestGroup);
    if (errorMessage === null)
    {
      let scimClient = new ScimClient2();
      let onSuccess = resource =>
      {
        addRequestGroup(resource);
        setAddNewEntry(false);
      };
      let onError = errorResponse => setErrorResponse(errorResponse);
      scimClient.createResource(HTTP_REQUEST_GROUPS_ENDPOINT, newRequestGroup, onSuccess, onError);
    }
    setErrorResponse(errorMessage);
  }

  return <>
    <ListGroup defaultActiveKey={"#"}>
      <ListGroup.Item variant={"warning"} draggable={false}>
        Request Groups
        <PlusLg className={"add-list-item-icon"}
                onClick={() => setAddNewEntry(true)}/>
      </ListGroup.Item>
      {
        addNewEntry &&
        <ListGroup.Item draggable={false}>
          <Form.Control type={"text"} ref={newRequestGroupName} className={"w-75 d-inline"}
                        onKeyUp={e =>
                        {
                          if (lodash.toLower(e.key) === 'enter')
                          {
                            saveNewRequestGroup();
                          }
                        }}/>
          <XLg className={"add-list-item-icon delete-icon edit"}
               onClick={() => setAddNewEntry(false)}/>
          <Save className={"add-list-item-icon save-icon listed-icon edit"}
                onClick={() => saveNewRequestGroup()}/>
          {
            new Optional(errorResponse).isPresent() &&
            <small className={"bg-danger error text-white d-block"}>
              <GoFlame/> {errorResponse}
            </small>
          }
        </ListGroup.Item>
      }
      <ListGroup.Item action
                      draggable={false}
                      href={"#"}
                      onClick={() => updateRequestGroupContext({})}>
        #
      </ListGroup.Item>
      {
        requestGroups.map(group =>
        {
          return <EditableListItem key={group.name}
                                   group={group}
                                   updateRequestGroup={updateRequestGroup}
                                   removeRequestGroup={removeRequestGroup}/>;
        })
      }
    </ListGroup>
  </>;
}

function EditableListItem({
                            group,
                            updateRequestGroup,
                            removeRequestGroup
                          })
{

  const [editMode, setEditMode] = useState(false);
  const [setErrorResponse] = useScimErrorResponse();

  const updateRequestGroupContext = useContext(RequestGroupUpdateContext);
  const groupRefName = useRef(group.name);

  function updateResourceOnServer()
  {
    let scimClient = new ScimClient2();
    let onSuccess = updatedGroup =>
    {
      setEditMode(false);
      updateRequestGroup(group, updatedGroup);
    };
    let onError = errorResponse => setErrorResponse(errorResponse);
    let newResource = lodash.cloneDeep(group);
    newResource.name = groupRefName.current.value;
    scimClient.updateResource(HTTP_REQUEST_GROUPS_ENDPOINT,
      group.id,
      newResource,
      onSuccess,
      onError);
  }

  return <ListGroup.Item key={group.name}
                         action
                         href={"#" + group.name}
                         draggable={false}
                         onClick={() => updateRequestGroupContext(group)}>
    {
      !editMode &&
      group.name
    }
    {
      editMode &&
      <Form.Control type={"text"} ref={groupRefName} defaultValue={group.name} className={"w-75 d-inline"}
                    onClick={e =>
                    {
                      e.stopPropagation();
                    }}
                    onKeyUp={e =>
                    {
                      if (e.key === 'Enter')
                      {
                        updateResourceOnServer();
                      }
                    }}/>
    }
    {
      !editMode &&
      <OverlayTrigger trigger={"click"} placement={"left"} rootClose={true}
                      overlay={<Popover className={"p-2 align-content-center"}>
                        <p>Delete entry?</p>
                        <Button variant={"danger"} className={"me-2"} onClick={e =>
                        {
                          let scimClient = new ScimClient2();
                          let onSuccess = () => removeRequestGroup(group);
                          let onError = errorResponse => setErrorResponse(errorResponse);
                          scimClient.deleteResource(HTTP_REQUEST_GROUPS_ENDPOINT,
                            group.id,
                            onSuccess,
                            onError);
                        }}>Yes</Button>
                        <Button variant={"secondary"} onClick={e =>
                        {
                          e.stopPropagation();
                          document.body.click(); // will close the popover
                        }}>No</Button>
                      </Popover>}
      >
        <Trash className={"add-list-item-icon save-icon edit http-category-icon ms-1 mt-2"}
               onClick={e =>
               {
                 e.stopPropagation();
               }}/>
      </OverlayTrigger>
    }
    {
      !editMode &&
      <PencilSquare className={"add-list-item-icon save-icon edit http-category-icon ms-1 mt-2"}
                    onClick={e =>
                    {
                      e.stopPropagation();
                      setEditMode(true);
                    }}/>
    }
    {
      editMode &&
      <XLg className={"add-list-item-icon save-icon edit http-category ms-1"}
           onClick={e =>
           {
             e.stopPropagation();
             setEditMode(false);
           }}/>
    }
    {
      editMode &&
      <Save className={"add-list-item-icon save-icon edit http-category ms-1"}
            onClick={e =>
            {
              e.stopPropagation();
              updateResourceOnServer();
            }}/>
    }
  </ListGroup.Item>;
}
