import React, {useEffect, useRef, useState} from "react";
import {ListGroup} from "react-bootstrap";
import {PencilSquare, PlusLg, Save, Trash, XLg} from "react-bootstrap-icons";
import {Optional} from "./utils";
import {ErrorMessageList} from "../base/form-base";
import Button from "react-bootstrap/Button";
import * as lodash from "lodash";

const INNER_MENU_ACTIONS = {
  SELECT: 'select',
  DESELECT: 'deselect',
  ADD: 'add',
  REMOVE: 'remove',
  UPDATE: 'update'
};

function reducer(entries, action, reducer)
{
  let optionalReducer = new Optional(reducer);
  switch (action.type)
  {
    case INNER_MENU_ACTIONS.SELECT:
      optionalReducer.map(f => f.select).ifPresent(f => f(action.payload));
      return;
    case INNER_MENU_ACTIONS.DESELECT:
      optionalReducer.map(f => f.deselect).ifPresent(f => f(action.payload));
      return;
    case INNER_MENU_ACTIONS.ADD:
      optionalReducer.map(f => f.add).ifPresent(f => f(action.payload));
      reducer.add(action.payload);
      return;
    case INNER_MENU_ACTIONS.UPDATE:
      optionalReducer.map(f => f.update).ifPresent(f => f(action.payload));
      reducer.update(action.payload);
      return;
    case INNER_MENU_ACTIONS.REMOVE:
      optionalReducer.map(f => f.remove).ifPresent(f => f(action.payload));
      reducer.remove(action.payload);
      return;
  }
}

export function InnerMenubar({
                               initialEntries,
                               onMenuEntryAdd,
                               onMenuEntryUpdate,
                               onMenuEntryDelete,
                               onClick,
                               header,
                               headerOff,
                               objectComparison,
                               objectConstructor
                             })
{
  // const [entries, dispatch] = useReducer((entries, action) => reducer(entries, action, reducerCallables),
  // initialEntries);

  const [menuEntryList, setMenuEntryList] = useState(initialEntries || []);
  const [showAddNewEntry, setShowAddNewEntry] = useState(false);
  const [activeIndex, setActiveIndex] = useState(menuEntryList.length > 0 ? 0 : -1);

  const rendered = useRef(1);

  useEffect(() =>
  {
    setMenuEntryList(initialEntries || []);
  }, [initialEntries]);

  useEffect(() =>
  {
    rendered.current = rendered.current + 1;
  });

  function addMenuEntry(value)
  {
    let newObject = objectConstructor(value);
    return addValueToStatefulArray(newObject, menuEntryList, setMenuEntryList, objectComparison, () =>
    {
      setShowAddNewEntry(false);
      if (onMenuEntryAdd)
      {
        onMenuEntryAdd(newObject);
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
        if (onMenuEntryDelete)
        {
          onMenuEntryDelete(value);
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
        if (onMenuEntryUpdate)
        {
          onMenuEntryUpdate(oldValue, newValue);
        }
      });
  }

  return <ListGroup>
    <React.Fragment>
      {
        new Optional(headerOff).isEmpty() &&
        <ListGroup.Item variant={"warning"}>
          <div>rendered: {rendered.current}</div>
          {header} <PlusLg className={"add-list-item-icon"}
                           onClick={() =>
                           {
                             setShowAddNewEntry(true);
                           }}/>
        </ListGroup.Item>
      }
      {
        showAddNewEntry &&
        <NewMenuEntry abort={() => setShowAddNewEntry(false)}
                      addMenuEntry={addMenuEntry}/>
      }
    </React.Fragment>
    {
      menuEntryList.map((entry, index) =>
      {
        return <MenuListItem key={entry.name}
                             active={index === activeIndex}
                             menuEntry={entry}
                             deleteMenuEntry={deleteMenuEntry}
                             updateMenuEntry={updateMenuEntry}
                             onClick={menuEnty =>
                             {
                               if (activeIndex === index)
                               {
                                 setActiveIndex(-1);
                                 onClick(null);
                               } else
                               {
                                 setActiveIndex(index);
                                 onClick(menuEnty);
                               }
                             }}/>;
      })
    }
  </ListGroup>;
}

function NewMenuEntry({abort, addMenuEntry})
{

  const menuEntryRef = useRef("");
  const [errors, setErrors] = useState([]);

  function add()
  {
    let errorMessage = addMenuEntry(menuEntryRef.current.value);
    new Optional(errorMessage).ifPresent(val => setErrors([val]));
  }

  return <ListGroup.Item variant="secondary">
    <input type={"text"}
           ref={menuEntryRef}
           autoFocus={true}
           className={"new-menu-entry-input"}
           onKeyUp={e =>
           {
             if (e.key === 'Enter')
             {
               add();
             }
           }}/>
    <XLg className={"add-list-item-icon delete-icon edit"}
         onClick={() =>
         {
           menuEntryRef.current.value = "";
           abort();
         }}/>
    <Save className={"add-list-item-icon save-icon listed-icon edit"}
          onClick={() => add()}/>
    {
      errors && errors.length !== 0 &&
      <div>
        <ErrorMessageList backgroundClass={"bg-danger menu-entry"} fieldErrors={errors}/>
      </div>
    }
  </ListGroup.Item>;
}

function MenuListItem(props)
{
  const [editMode, setEditMode] = useState(false);
  const [deleteMode, setDeleteMode] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [menuEntry, setMenuEntry] = useState(props.menuEntry);

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
               onClick={e => setMenuEntry(e.target.value)}
               onKeyUp={e =>
               {
                 if (e.key === 'Enter')
                 {
                   updateMenuEntry(menuEntry);
                 }
               }}/>
        <XLg className={"add-list-item-icon save-icon edit http-category ms-1"}
             onClick={() =>
             {
               setEditMode(false);
               menuEntry(props.menuEntry);
               setErrorMessage(null);
             }}/>
        <Save className={"add-list-item-icon save-icon edit http-category ms-1"}
              onClick={() =>
              {
                updateMenuEntry(menuEntry);
              }}/>
        {
          errorMessage != null &&
          <ErrorMessageList fieldErrors={[errorMessage]}/>
        }
      </div>
    }
    {
      !editMode &&
      <React.Fragment>
                <span className={"clickable-inner-menu-entry"}
                      onClick={() => props.onClick && props.onClick(props.menuEntry)}>
                  {props.menuEntry.name}
                </span>
        <Trash className={"add-list-item-icon save-icon edit http-category-icon ms-1 mt-2"}
               onClick={e => setDeleteMode(true)}
        />
        {
          !editMode &&
          <PencilSquare className={"add-list-item-icon save-icon edit http-category-icon ms-1 mt-2"}
                        onClick={() => setEditMode(true)}/>
        }
        {
          deleteMode &&
          <DeleteMenuEntryBlock deleteMenuEntry={() => props.deleteMenuEntry(props.menuEntry)}
                                setDeleteMode={setDeleteMode}/>
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

function addValueToStatefulArray(value, statefulArray, setStatefulArray, objectComparison, afterSuccess)
{
  let duplicateObject = new Optional(lodash.find(statefulArray, value, objectComparison));
  if (duplicateObject.isPresent())
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
  newArray.sort(objectComparison);
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
