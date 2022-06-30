# Video Backend Service Endpoints

## All videos

<tt>GET localhost/api/admin/video</tt>

## Single video

<tt>GET localhost/api/admin/video/1</tt>

## Video thumbnail image

<tt>GET localhost/api/admin/video/image/1</tt>

## Add video

<tt>POST localhost/api/admin/video</tt>

```json
 {
    "id": 0,
    "name": "posted2",
    "source": "YT gen",
    "link": "some link",
    "description": "Enjoyed no sooner but despised straight",
    "hidden": false,
    "noOfViews": 1100,
    "noOfUpvotes": 30,
    "noOfDownvotes": 12
  }
```
     
returns the new record   
   
## Update video     

<tt>PUT localhost/api/admin/video</tt>

```json
{
    "id": 619,
    "name": "updated",
    "source": "YT gen",
    "link": "some link update",
    "description": "Enjoyed no sooner but despised straight, update",
    "hidden": false,
    "noOfViews": 1101,
    "noOfUpvotes": 31,
    "noOfDownvotes": 13
}
```
     
returns the updated record

## Update the thumbnail

<tt>PUT localhost/api/admin/video/image/1?url=https://www.guidingeyes.org/wp-content/uploads/2020/01/1-1.jpg</tt>

## Delete video

<tt>DELETE localhost/api/admin/video/1</tt>

returns 1 if deleted, 0 if no record with this id

## Delete Multiple Videos (Batch Delete)

NOTE: Deletes video/tag + video/category "links" internally

<tt>DELETE localhost/api/admin/videos?ids=1,2,3</tt>

returns 1 if deleted, 0 if the delete failed

NOTE: Deletes video/tag + video/category "links" internally

## Populate videos with up to 100 random videos (demo/testing endpoint)

<tt>GET localhost/api/admin/video/populate</tt>

## Populate videos with up to n random videos (demo/testing endpoint)

<tt>GET localhost/api/admin/video/populate/3</tt>

## Tags for a video

<tt>GET localhost/api/admin/video/tags/1</tt>
=> array of tag Ids and naames

## Categories for a video

<tt>GET localhost/api/admin/video/categories/1</tt>
=> array of category Ids and naames

## Set Tags for a video

<tt>POST localhost/api/admin/video/tags/1?tags=1,2,3</tt>
=> array of tag Ids

## Set Categories for a video

<tt>POST localhost/api/admin/video/categories/1?categories=1,2,3</tt>
=> array of category Ids

## Most recent videos (optional params page (default 1) and pagesize (default 10)

<tt>GET localhost/api/admin/video/orderBy/upload</tt>

## Most popular videos (optional params page (default 1) and pagesize (default 10)

<tt>GET localhost/api/admin/video/orderBy/upvotes</tt>