# Tag Backend Service Endpoints

## All tags

<tt>GET localhost/api/admin/tag</tt>

## Single tag

<tt>GET localhost/api/admin/tag/1</tt>

## Add tag

<tt>POST localhost/api/admin/tag</tt>

```json
{
  "id": "0",
  "name": "Documentary"
}
```
     
returns the new record   
   
## Update tag

<tt>PUT localhost/api/admin/tag</tt>

```json
{
  "id": "0",
  "name": "News"
}
```
     
returns the updated record

## Delete tag

<tt>DELETE localhost/api/admin/tag</tt>

returns 1 if deleted, 0 if no record with this id

## Populate tags with up to 100 random tags (demo/testing endpoint)

<tt>GET localhost/api/admin/tag/populate</tt>

## Videos for a tag

<tt>GET localhost/api/admin/tag/videos/1</tt>
=> array of videos