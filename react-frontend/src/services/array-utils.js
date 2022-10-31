import {useState} from "react";
import * as lodash from "lodash";
import {Optional} from "./utils";

export function useUniqueArray(initialArray, comparisonAttribute, sortOverride)
{
    const [array, setArrayInternal] = useState(initialArray.sort(sort) || []);
    
    function sort(c1, c2)
    {
        if (sortOverride === undefined || sortOverride === null)
        {
            return comparisonAttribute(c1).localeCompare(comparisonAttribute(c2));
        }
        else
        {
            sortOverride(comparisonAttribute(c1), comparisonAttribute(c2));
        }
    }
    
    function equals(c1, c2)
    {
        return comparisonAttribute(c1) === comparisonAttribute(c2);
    }
    
    function setArray(newArray)
    {
        newArray.sort(sort);
        setArrayInternal(newArray);
    }
    
    function isInsertable(element)
    {
        if (new Optional(comparisonAttribute(element)).map(s => s.trim() === '').get())
        {
            return "empty objects not allowed";
        }
        
        let indexOf = lodash.findIndex(array, o => equals(element, o));
        if (indexOf !== -1)
        {
            return "duplicate element";
        }
        return null;
    }
    
    function add(element)
    {
        let copiedArray = [...array];
        copiedArray.push(element);
        setArray(copiedArray);
        return copiedArray;
    }
    
    function update(oldElement, newElement)
    {
        let indexOfOld = lodash.findIndex(array, o => equals(oldElement, o));
        let copiedArray = [...array];
        copiedArray.splice(indexOfOld, 1, newElement);
        setArray(copiedArray);
        return true;
    }
    
    function remove(element)
    {
        let indexOf = lodash.findIndex(array, o => equals(element, o));
        let copiedArray = [...array];
        copiedArray.splice(indexOf, 1);
        setArray(copiedArray);
        return true;
    }
    
    return [array, setArray, isInsertable, add, update, remove];
}
