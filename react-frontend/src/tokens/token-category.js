import React, {useEffect, useState} from "react";
import {TokenCategoryClient} from "./token-category-client";
import {Alert, Button, Col, Form, ListGroup, Row, Tab} from "react-bootstrap";
import {ArrowRightCircle, PencilSquare, PlusLg, Save, Trash, XLg} from "react-bootstrap-icons";
import {GoFlame} from "react-icons/go";
import {TokenStoreList} from "./token-store-list";

export function TokenCategoryList()
{
  
  const [errors, setErrors] = useState({});
  const [filter, setFilter] = useState("");
  const [loadedOnce, setloadedOnce] = useState(false);
  const [totalResults, setTotalResults] = useState(0);
  const [tokenCategoryList, setTokenCategoryList] = useState([]);
  const [tokensRemovedByFilter, setTokensRemovedByFilter] = useState([]);
  
  function addNewCategories(categoryArray)
  {
    let newTokenCategories = [...tokenCategoryList, ...categoryArray];
    newTokenCategories.sort((c1, c2) => c1.name.localeCompare(c2.name));
    setTokenCategoryList(newTokenCategories);
  }
  
  function updateCategory(oldCategory, newCategory)
  {
    let copiedList = [...tokenCategoryList];
    let indexOf = copiedList.indexOf(oldCategory);
    copiedList.splice(indexOf, 1, newCategory);
    setTokenCategoryList([...copiedList]);
  }
  
  function removeCategory(category)
  {
    let copiedList = [...tokenCategoryList];
    let index = copiedList.indexOf(category);
    copiedList.splice(index, 1);
    setTotalResults(totalResults - 1);
    setTokenCategoryList([...copiedList]);
  }
  
  function setCategoryEntires(category, numberOfEntries)
  {
    category.numberOfEntries = numberOfEntries;
    let copiedList = [...tokenCategoryList];
    setTokenCategoryList([...copiedList]);
  }
  
  useEffect(() =>
            {
              let searchRequest = {
                startIndex: tokenCategoryList.length,
                sortBy: "name"
              };
    
              function onSuccess(listResponse)
              {
                setTotalResults(listResponse.totalResults);
                let newResources = listResponse.Resources || [];
                addNewCategories([...newResources]);
                setloadedOnce(true);
              }
    
              function onError(errorResponse)
              {
                setErrors(errorResponse);
              }
    
              if ((totalResults === 0 && !loadedOnce) || tokenCategoryList.length < totalResults)
              {
                new TokenCategoryClient().listCategories(searchRequest, onSuccess, onError);
              }
            }, [tokenCategoryList]);
  
  useEffect(() =>
            {
    
            }, [filter]);
  
  return <React.Fragment>
    {
      errors && errors.length > 0 &&
      <Alert variant={"danger"}>
        <Form.Text>
          {JSON.stringify(errors)}
        </Form.Text>
      </Alert>
    }
    <Tab.Container id="list-group-tabs-example">
      <Row>
        <Col sm={3}>
          <h5>found a total of {totalResults} categories</h5>
        </Col>
        <Col>
          <div className={"filter-block"}>
              <span>
                <span>search for token part: </span>
                <input id={"filter-input"}
                       onKeyUp={e =>
                       {
                         if (e.key === 'Enter')
                         {
                           setFilter(document.getElementById("filter-input").value);
                         }
                       }} />
                <Button onClick={e => setFilter(document.getElementById("filter-input").value)}
                >search</Button>
              </span>
          </div>
        </Col>
      </Row>
      <Row>
        <Col sm={3}>
          <ListGroup>
            <CategoryHeader addToTokenCategoryList={resource =>
            {
              setTotalResults(totalResults + 1);
              addNewCategories([resource]);
            }} />
            {
              tokenCategoryList &&
              tokenCategoryList.map(tokenCategory =>
                                    {
                                      return <CategoryListItem key={tokenCategory.id}
                                                               category={tokenCategory}
                                                               updateCategory={updateCategory}
                                                               removeCategory={removeCategory} />;
                                    })
            }
          </ListGroup>
        </Col>
        <Col sm={9}>
          <Tab.Content>
            {
              tokenCategoryList &&
              tokenCategoryList.map(tokenCategory =>
                                    {
                                      return <Tab.Pane key={tokenCategory.id}
                                                       eventKey={"#" + tokenCategory.id}>
                                        <TokenStoreList category={tokenCategory} filter={filter}
                                                        setcategoryEntires={setCategoryEntires} />
                                      </Tab.Pane>;
                                    })
            }
          </Tab.Content>
        </Col>
      </Row>
    </Tab.Container>
  </React.Fragment>;
}

function CategoryListItem(props)
{
  
  const [errors, setErrors] = useState({});
  const [editMode, setEditMode] = useState(false);
  const [deleteMode, setDeleteMode] = useState(false);
  const [value, setValue] = useState(props.category.name);
  
  function updateCategory()
  {
    setErrors(null);
    
    function onSuccess(newCategory)
    {
      props.updateCategory(props.category, newCategory);
    }
    
    if (props.category.name !== value)
    {
      new TokenCategoryClient().updateCategory(props.category.id,
                                               value,
                                               onSuccess,
                                               errorResponse => setErrors(errorResponse));
    }
  }
  
  function deleteCategory()
  {
    setErrors(null);
    
    function onSuccess(resource)
    {
      props.removeCategory(resource);
    }
    
    new TokenCategoryClient().deleteCategory(props.category, onSuccess, errorResponse => setErrors(errorResponse));
  }
  
  let numberOfEntries = props.category.numberOfEntries || 0;
  
  return <ListGroup.Item variant={(numberOfEntries === 0) ? "dark" : "light"}
                         action
                         href={"#" + props.category.id}
                         onKeyDown={e => editMode && e.stopPropagation()}>
    <ArrowRightCircle style={{margin: "0 10px 0 0"}} />
    {
      !editMode &&
      <React.Fragment>
        {value} [{numberOfEntries}]
      </React.Fragment>
    }
    {
      editMode &&
      <input type={"text"}
             className={"listed-category-header-input"}
             value={value}
             onChange={e => setValue(e.target.value)}
             onKeyUp={e =>
             {
               if (e.key === 'Enter')
               {
                 updateCategory();
                 setEditMode(!editMode);
               }
               else if (e.key === 'Escape')
               {
                 setValue(props.category.name);
                 setEditMode(false);
               }
             }} />
    }
    <Trash className={"add-list-item-icon delete-icon"}
           onClick={() => setDeleteMode(!deleteMode)} />
    {
      editMode &&
      <XLg className={"add-list-item-icon abort-icon listed-icon"}
           onClick={() =>
           {
             setValue(props.category.name);
             setEditMode(false);
           }} />
    }
    <EditIcon editMode={editMode}
              classNames={"add-list-item-icon edit-icon listed-icon"}
              onClick={() =>
              {
                if (editMode)
                {
                  props.category.name = value;
                  updateCategory();
                }
                setEditMode(!editMode);
              }} />
    {
      deleteMode &&
      <DeleteCategoryBlock deleteMode={deleteMode} setDeleteMode={setDeleteMode} deleteCategory={deleteCategory} />
    }
    {
      errors && errors.detail &&
      <Alert variant={"danger"}>
        <Form.Text>
          <GoFlame /> {errors.detail}
        </Form.Text>
      </Alert>
    }
  </ListGroup.Item>;
}

function DeleteCategoryBlock(props)
{
  
  return <div className={"list-delete-insertion"}>
    <div className={"list-delete-text"}>
      delete category?
    </div>
    <Button variant={"danger"}
            className={"listed-icon list-button"}
            onClick={() =>
            {
              props.deleteCategory();
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

function EditIcon(props)
{
  return <React.Fragment>
    {
      !props.editMode &&
      <PencilSquare className={props.classNames}
                    onClick={props.onClick} />
    }
    {
      props.editMode &&
      <Save className={props.classNames}
            onClick={props.onClick} />
    }
  </React.Fragment>;
}

function CategoryHeader(props)
{
  
  const [errors, setErrors] = useState({});
  const [category, setCategory] = useState();
  
  function saveNewCategory()
  {
    function onSaveSuccess(category)
    {
      props.addToTokenCategoryList(category);
      setCategory(null);
      setErrors(null);
    }
    
    function onSaveError(errorResponse)
    {
      setErrors(errorResponse);
    }
    
    new TokenCategoryClient().createCategory(category, onSaveSuccess, onSaveError);
  }
  
  return <React.Fragment>
    <ListGroup.Item variant={"warning"}>
      Categories <PlusLg className={"add-list-item-icon"}
                         onClick={() =>
                         {
                           if (!category)
                           {
                             setCategory("new_category");
                           }
                         }} />
    </ListGroup.Item>
    {
      category &&
      <ListGroup.Item variant="secondary">
        <input type={"text"}
               className={"list-item-input"}
               value={category}
               onChange={e => setCategory(e.target.value)}
               onKeyUp={e =>
               {
                 if (e.key === 'Enter')
                 {
                   saveNewCategory();
                 }
               }} />
        <XLg className={"add-list-item-icon delete-icon edit"}
             onClick={() => setCategory(null)} />
        <Save className={"add-list-item-icon save-icon listed-icon edit"}
              onClick={() => saveNewCategory()} />
        {
          errors && errors.detail &&
          <Alert variant={"danger"}>
            <Form.Text>
              <GoFlame /> {errors.detail}
            </Form.Text>
          </Alert>
        }
      </ListGroup.Item>
    }
  </React.Fragment>;
}
