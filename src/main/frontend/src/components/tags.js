import React from "react";

const Tags = ({ type, tags, handleTagChange, handleAddTag, handleRemoveTag }) => {
  return (
    <div className={`${type}-tags-container tags-container`}>
      {tags.map((tag, index) => (
        <div key={index} className="tag-row">
          <input
            type="text"
            placeholder="Tag Key"
            value={tag.key}
            onChange={(e) => handleTagChange(index, "key", e.target.value, type)}
          />
          <input
            type="text"
            placeholder="Tag Value"
            value={tag.value}
            onChange={(e) => handleTagChange(index, "value", e.target.value, type)}
          />
          <button
            className="remove-button"
            onClick={() => handleRemoveTag(index, type)}
          >
            x
          </button>
        </div>
      ))}
      <button
        className="addTag-button"
        onClick={() => handleAddTag(type)}
      >
        태그 추가
      </button>
    </div>
  );
};

export default Tags;
