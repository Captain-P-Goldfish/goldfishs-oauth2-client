import React, {useEffect, useState} from "react";
import {ListGroup} from "react-bootstrap";
import {PencilSquare, PlusLg, Save, Trash, XLg} from "react-bootstrap-icons";
import {Optional} from "./utils";
import {ErrorMessageList} from "../base/form-base";
import Button from "react-bootstrap/Button";
import * as lodash from "lodash";

export function InnerMenubar(props)
{
  const [menuEntryList, setMenuEntryList] = useState(props.entries || []);
  const [showAddNewEntry, setShowAddNewEntry] = useState(false);
  const [activeIndex, setActiveIndex] = useState(menuEntryList.length > 0 ? 0 : -1);
  
  useEffect(() => {
    setMenuEntryList(props.entries);
  })
  
  function addMenuEntry(value)
  {
    return addValueToStatefulArray(value, menuEntryList, setMenuEntryList, () =>
                                   {
                                     setShowAddNewEntry(false);
                                     if (props.onMenuEntryAdd)
                                     {
                                       props.onMenuEntryAdd(value);
                                     }
                                   });
  }
  
  function deleteMenuEntry(value)
  {
    return removeValueFromStatefulArray(value,
                                        menuEntryList,
                                        setMenuEntryList,
                                        () =>
                                        {
                                          if (props.onMenuEntryDelete)
                                          {
                                            props.onMenuEntryDelete(value);
                                          }
                                        });
  }
  
  function updateMenuEntry(oldValue, newValue)
  {
    return updateValueInStatefulArray(oldValue,
                                      newValue,
                                      menuEntryList,
                                      setMenuEntryList,
                                      () =>
                                      {
                                        if (props.onMenuEntryUpdate)
                                        {
                                          props.onMenuEntryUpdate(oldValue, newValue);
                                        }
                                      });
  }
  
  return <ListGroup>
    <ListGroup.Item variant={"warning"}>
      {props.header} <PlusLg className={"add-list-item-icon"}
                             onClick={() =>
                             {
                               setShowAddNewEntry(true);
                             }} />
    </ListGroup.Item>
    {
      showAddNewEntry &&
      <NewMenuEntry abort={() => setShowAddNewEntry(false)}
                    addMenuEntry={addMenuEntry} />
    }
    {
      menuEntryList.map((entry, index) =>
                        {
                          return <MenuListItem key={entry}
                                               active={index === activeIndex}
                                               menuEntry={entry}
                                               deleteMenuEntry={deleteMenuEntry}
                                               updateMenuEntry={updateMenuEntry}
                                               onClick={menuEnty =>
                                               {
                                                 setActiveIndex(index);
                                                 props.onClick(menuEnty);
                                               }} />;
                        })
    }
  </ListGroup>;
}

function NewMenuEntry(props)
{
  
  const [menuEntry, setMenuEntry] = useState("");
  const [errors, setErrors] = useState([]);
  
  function addMenuEntry()
  {
    let errorMessage = props.addMenuEntry(menuEntry);
    new Optional(errorMessage).ifPresent(val => setErrors([val]));
  }
  
  return <ListGroup.Item variant="secondary">
    <input type={"text"}
           autoFocus={true}
           className={"new-menu-entry-input"}
           value={menuEntry}
           onChange={e => setMenuEntry(e.target.value)}
           onKeyUp={e =>
           {
             if (e.key === 'Enter')
             {
               addMenuEntry();
             }
           }} />
    <XLg className={"add-list-item-icon delete-icon edit"}
         onClick={() =>
         {
           setMenuEntry(null);
           props.abort();
         }} />
    <Save className={"add-list-item-icon save-icon listed-icon edit"}
          onClick={() => addMenuEntry()} />
    {
      errors && errors.length !== 0 &&
      <div>
        <ErrorMessageList backgroundClass={"bg-danger menu-entry"} fieldErrors={errors} />
      </div>
    }
  </ListGroup.Item>;
}

function MenuListItem(props)
{
  const [editMode, setEditMode] = useState(false);
  const [deleteMode, setDeleteMode] = useState(false);
  const [menuEntry, setMenuEntry] = useState(props.menuEntry);
  const [errorMessage, setErrorMessage] = useState(null);
  
  function updateMenuEntry(menuEntry)
  {
    setErrorMessage(null);
    setMenuEntry(menuEntry);
    let errorMessage = props.updateMenuEntry(props.menuEntry, menuEntry);
    let optional = new Optional(errorMessage);
    optional.ifPresent(val => setErrorMessage(val));
    optional.ifNotPresent(() => setEditMode(false));
  }
  
  return <ListGroup.Item className={props.active ? "active-menu-entry" : ""}>
    {
      editMode &&
      <div>
        <input className={"new-menu-entry-input w-75 m-0"}
               autoFocus={true}
               value={menuEntry}
               onChange={e => setMenuEntry(e.target.value)}
               onKeyUp={e =>
               {
                 if (e.key === 'Enter')
                 {
                   updateMenuEntry(menuEntry);
                 }
               }} />
        <XLg className={"add-list-item-icon save-icon edit http-category ms-1"}
             onClick={() =>
             {
               setEditMode(false);
               setMenuEntry(props.menuEntry);
               setErrorMessage(null);
             }} />
        <Save className={"add-list-item-icon save-icon edit http-category ms-1"}
              onClick={() =>
              {
                updateMenuEntry(menuEntry);
              }} />
        {
          errorMessage != null &&
          <ErrorMessageList fieldErrors={[errorMessage]} />
        }
      </div>
    }
    {
      !editMode &&
      <React.Fragment>
        <span className={"clickable-inner-menu-entry"} onClick={() => props.onClick && props.onClick(props.menuEntry)}>
          {props.menuEntry}
        </span>
        <Trash className={"add-list-item-icon save-icon edit http-category-icon ms-1 mt-2"}
               onClick={e => setDeleteMode(true)}
        />
        {
          !editMode &&
          <PencilSquare className={"add-list-item-icon save-icon edit http-category-icon ms-1 mt-2"}
                        onClick={() => setEditMode(true)} />
        }
        {
          deleteMode &&
          <DeleteMenuEntryBlock deleteMenuEntry={() => props.deleteMenuEntry(props.menuEntry)}
                                setDeleteMode={setDeleteMode} />
        }
      </React.Fragment>
    }
  </ListGroup.Item>;
}

function DeleteMenuEntryBlock(props)
{
  return <div className={"list-delete-insertion"}>
    <div className={"list-delete-text"}>
      Are you sure?
    </div>
    <Button variant={"danger"}
            className={"listed-icon list-button"}
            onClick={() =>
            {
              props.deleteMenuEntry();
              props.setDeleteMode(false);
            }}>
      Yes
    </Button>
    <Button variant={"secondary"} className={"listed-icon"} onClick={() =>
    {
      props.setDeleteMode(false);
    }}>
      No
    </Button>
  </div>;
}


function addValueToStatefulArray(value, statefulArray, setStatefulArray, afterSuccess)
{
  let indexOf = statefulArray.indexOf(value);
  if (indexOf !== -1)
  {
    return "duplicate value";
  }
  let newValue = lodash.trim(value, " ");
  newValue = newValue ? newValue : null;
  if (newValue === null)
  {
    return null;
  }
  let newArray = [...statefulArray, newValue];
  newArray.sort((c1, c2) => c1.localeCompare(c2));
  setStatefulArray(newArray);
  if (afterSuccess !== undefined && afterSuccess !== null)
  {
    afterSuccess();
  }
}

function updateValueInStatefulArray(oldValue, value, statefulArray, setStatefulArray, afterSuccess)
{
  if (oldValue === value)
  {
    return;
  }
  let foundObject = lodash.find(statefulArray, value);
  if (foundObject !== undefined)
  {
    return "duplicate value";
  }
  let newValue = lodash.trim(value, " ");
  newValue = oldValue ? newValue : null;
  if (newValue === null)
  {
    return;
  }
  let newArray = [...statefulArray];
  let indexOf = newArray.indexOf(oldValue);
  newArray.splice(indexOf, 1, newValue);
  newArray.sort((c1, c2) => c1.localeCompare(c2));
  setStatefulArray(newArray);
  if (afterSuccess !== undefined && afterSuccess !== null)
  {
    afterSuccess();
  }
}

function removeValueFromStatefulArray(value, statefulArray, setStatefulArray, afterSuccess)
{
  let newValue = lodash.trim(value, " ");
  newValue = newValue ? newValue : null;
  if (newValue === null)
  {
    return null;
  }
  let newArray = [...statefulArray];
  let indexOf = newArray.indexOf(value);
  newArray.splice(indexOf, 1);
  setStatefulArray(newArray);
  if (afterSuccess !== undefined && afterSuccess !== null)
  {
    afterSuccess();
  }
}
