# Category Backend Service Endpoints

## All categories

<tt>GET localhost/api/admin/category</tt>

## Single category

<tt>GET localhost/api/admin/category/1</tt>

## Add category

<tt>POST localhost/api/admin/category</tt>

```json
{
  "id": "0",
  "name": "Doom eternal games"
}
```
     
returns the new record   
   
## Update category

<tt>PUT localhost/api/admin/category</tt>

```json
{
  "id": "0",
  "name": "Doom eternal games"
}
```
     
returns the updated record

## Delete category

<tt>DELETE localhost/api/admin/category</tt>

returns 1 if deleted, 0 if no record with this id

## Populate categories with up to 100 random categories (demo/testing endpoint)

<tt>GET localhost/api/admin/category/populate</tt>

## Videos for a Category

<tt>GET localhost/api/admin/category/videos/1?page=2</tt>
